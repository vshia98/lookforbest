import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:go_router/go_router.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:model_viewer_plus/model_viewer_plus.dart';

import '../../../shared/providers/providers.dart';
import '../../../shared/widgets/error_view.dart';
import '../../../shared/widgets/loading_indicator.dart';
import '../../../shared/widgets/robot_card.dart';
import '../domain/robot.dart';

class RobotDetailPage extends HookConsumerWidget {
  final String id;

  const RobotDetailPage({super.key, required this.id});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final robotService = ref.read(robotServiceProvider);
    final dioClient = ref.read(dioClientProvider);

    final robotId = int.parse(id);

    final loading = useState(true);
    final robot = useState<Robot?>(null);
    final similarRobots = useState<List<RobotSummary>>([]);
    final isFavorited = useState(false);
    final selectedTab = useState(0); // 0=图片, 1=3D
    final error = useState<String?>(null);

    final descExpanded = useState(false);
    final specsExpanded = useState(false);
    final pageController = usePageController();
    final currentImagePage = useState(0);

    Future<void> loadData() async {
      loading.value = true;
      error.value = null;
      try {
        final results = await Future.wait([
          robotService.getRobotById(robotId),
          robotService.getSimilarRobots(robotId),
        ]);
        robot.value = results[0] as Robot;
        similarRobots.value = results[1] as List<RobotSummary>;
        isFavorited.value = (results[0] as Robot).isFavorited;
      } catch (e) {
        error.value = e.toString();
      } finally {
        loading.value = false;
      }
    }

    useEffect(() {
      loadData();
      return null;
    }, const []);

    Future<void> toggleFavorite() async {
      final r = robot.value;
      if (r == null) return;
      final prev = isFavorited.value;
      isFavorited.value = !prev;
      try {
        if (prev) {
          await dioClient.dio.delete('/users/me/favorites/$robotId');
        } else {
          await dioClient.dio.post('/users/me/favorites/$robotId');
        }
      } catch (_) {
        // 失败时回滚
        isFavorited.value = prev;
      }
    }

    if (loading.value) {
      return const Scaffold(body: LoadingIndicator(text: '加载中...'));
    }

    if (error.value != null) {
      return Scaffold(
        appBar: AppBar(),
        body: ErrorView(message: error.value!, onRetry: loadData),
      );
    }

    final r = robot.value!;
    final theme = Theme.of(context);
    final hasImages = r.images.isNotEmpty;
    final showTabs = r.has3dModel;

    // 当前展示内容：图片或3D
    Widget buildMediaSection() {
      if (showTabs && selectedTab.value == 1 && r.model3dUrl != null) {
        return ModelViewer(
          src: r.model3dUrl!,
          alt: r.name,
          ar: false,
          autoRotate: true,
          cameraControls: true,
        );
      }

      if (hasImages) {
        return Stack(
          children: [
            PageView.builder(
              controller: pageController,
              itemCount: r.images.length,
              onPageChanged: (i) => currentImagePage.value = i,
              itemBuilder: (context, index) {
                return CachedNetworkImage(
                  imageUrl: r.images[index].url,
                  fit: BoxFit.cover,
                  placeholder: (context, url) => Container(
                    color: theme.colorScheme.surfaceContainerHighest,
                    child: const Center(child: CircularProgressIndicator()),
                  ),
                  errorWidget: (context, url, error) => _buildImagePlaceholder(theme),
                );
              },
            ),
            // 图片指示器
            if (r.images.length > 1)
              Positioned(
                bottom: 12,
                left: 0,
                right: 0,
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: List.generate(
                    r.images.length,
                    (i) => AnimatedContainer(
                      duration: const Duration(milliseconds: 200),
                      margin: const EdgeInsets.symmetric(horizontal: 3),
                      width: currentImagePage.value == i ? 16 : 6,
                      height: 6,
                      decoration: BoxDecoration(
                        color: currentImagePage.value == i
                            ? Colors.white
                            : Colors.white54,
                        borderRadius: BorderRadius.circular(3),
                      ),
                    ),
                  ),
                ),
              ),
          ],
        );
      }

      if (r.coverImageUrl != null) {
        return CachedNetworkImage(
          imageUrl: r.coverImageUrl!,
          fit: BoxFit.cover,
          placeholder: (context, url) => Container(
            color: theme.colorScheme.surfaceContainerHighest,
          ),
          errorWidget: (context, url, error) => _buildImagePlaceholder(theme),
        );
      }

      return _buildImagePlaceholder(theme);
    }

    // 规格参数映射
    final specsMap = <String, String>{
      if (r.specs.payloadKg != null) '载荷': '${r.specs.payloadKg} kg',
      if (r.specs.reachMm != null) '臂展': '${r.specs.reachMm} mm',
      if (r.specs.dof != null) '自由度': '${r.specs.dof} DOF',
      if (r.specs.repeatabilityMm != null) '重复精度': '${r.specs.repeatabilityMm} mm',
      if (r.specs.maxSpeedDegS != null) '最大速度': '${r.specs.maxSpeedDegS} °/s',
      if (r.specs.weightKg != null) '整机重量': '${r.specs.weightKg} kg',
      if (r.specs.ipRating != null) 'IP防护': r.specs.ipRating!,
      if (r.specs.mounting != null) '安装方式': r.specs.mounting!,
    };

    final description = r.description ?? '';
    final descShort = description.length > 100
        ? description.substring(0, 100)
        : description;

    return Scaffold(
      body: Stack(
        children: [
          CustomScrollView(
            slivers: [
              // SliverAppBar 带图片/3D 展示
              SliverAppBar(
                expandedHeight: 300,
                pinned: true,
                leading: Padding(
                  padding: const EdgeInsets.all(6),
                  child: CircleAvatar(
                    backgroundColor: Colors.black38,
                    child: IconButton(
                      icon: const Icon(Icons.arrow_back, color: Colors.white),
                      onPressed: () => context.pop(),
                    ),
                  ),
                ),
                // 3D / 图片 切换 Tab（仅 has3dModel 时显示）
                actions: showTabs
                    ? [
                        Container(
                          margin: const EdgeInsets.only(right: 12, top: 8, bottom: 8),
                          decoration: BoxDecoration(
                            color: Colors.black45,
                            borderRadius: BorderRadius.circular(20),
                          ),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              _TabButton(
                                label: '图片',
                                selected: selectedTab.value == 0,
                                onTap: () => selectedTab.value = 0,
                              ),
                              _TabButton(
                                label: '3D',
                                selected: selectedTab.value == 1,
                                onTap: () => selectedTab.value = 1,
                              ),
                            ],
                          ),
                        ),
                      ]
                    : null,
                flexibleSpace: FlexibleSpaceBar(
                  background: Stack(
                    fit: StackFit.expand,
                    children: [
                      buildMediaSection(),
                      // 顶部半透明遮罩（保证 AppBar 图标可读）
                      Positioned(
                        top: 0,
                        left: 0,
                        right: 0,
                        height: 100,
                        child: Container(
                          decoration: const BoxDecoration(
                            gradient: LinearGradient(
                              begin: Alignment.topCenter,
                              end: Alignment.bottomCenter,
                              colors: [Colors.black54, Colors.transparent],
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),

              // 主内容
              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // 名称 + 收藏按钮
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  r.name,
                                  style: theme.textTheme.headlineSmall?.copyWith(
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                                if (r.modelNumber.isNotEmpty) ...[
                                  const SizedBox(height: 4),
                                  Text(
                                    r.modelNumber,
                                    style: theme.textTheme.bodyMedium?.copyWith(
                                      color: theme.colorScheme.onSurfaceVariant,
                                    ),
                                  ),
                                ],
                              ],
                            ),
                          ),
                          FloatingActionButton.small(
                            heroTag: 'favorite_fab',
                            onPressed: toggleFavorite,
                            backgroundColor: isFavorited.value
                                ? theme.colorScheme.primaryContainer
                                : theme.colorScheme.surfaceContainerHighest,
                            child: Icon(
                              isFavorited.value
                                  ? Icons.favorite
                                  : Icons.favorite_border,
                              color: isFavorited.value
                                  ? theme.colorScheme.primary
                                  : theme.colorScheme.onSurfaceVariant,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),

                      // 厂商信息卡片
                      Card(
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Padding(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 16,
                            vertical: 12,
                          ),
                          child: Row(
                            children: [
                              if (r.manufacturer.logoUrl != null) ...[
                                ClipRRect(
                                  borderRadius: BorderRadius.circular(6),
                                  child: CachedNetworkImage(
                                    imageUrl: r.manufacturer.logoUrl!,
                                    width: 40,
                                    height: 40,
                                    fit: BoxFit.contain,
                                    errorWidget: (_, __, ___) => const SizedBox(
                                      width: 40,
                                      height: 40,
                                    ),
                                  ),
                                ),
                                const SizedBox(width: 12),
                              ],
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      r.manufacturer.name,
                                      style: theme.textTheme.titleSmall?.copyWith(
                                        fontWeight: FontWeight.w600,
                                      ),
                                    ),
                                    if (r.manufacturer.country != null)
                                      Text(
                                        r.manufacturer.country!,
                                        style: theme.textTheme.bodySmall?.copyWith(
                                          color: theme.colorScheme.onSurfaceVariant,
                                        ),
                                      ),
                                  ],
                                ),
                              ),
                              Container(
                                padding: const EdgeInsets.symmetric(
                                  horizontal: 10,
                                  vertical: 4,
                                ),
                                decoration: BoxDecoration(
                                  color: theme.colorScheme.secondaryContainer,
                                  borderRadius: BorderRadius.circular(12),
                                ),
                                child: Text(
                                  r.category.name,
                                  style: theme.textTheme.labelSmall?.copyWith(
                                    color: theme.colorScheme.onSecondaryContainer,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                      const SizedBox(height: 16),

                      // 核心参数速览 Chips
                      if (r.specs.payloadKg != null ||
                          r.specs.reachMm != null ||
                          r.specs.dof != null) ...[
                        Text(
                          '核心参数',
                          style: theme.textTheme.titleSmall?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 8),
                        Wrap(
                          spacing: 8,
                          runSpacing: 8,
                          children: [
                            if (r.specs.payloadKg != null)
                              _SpecChipWidget(
                                icon: Icons.fitness_center,
                                label: '载荷',
                                value: '${r.specs.payloadKg} kg',
                              ),
                            if (r.specs.reachMm != null)
                              _SpecChipWidget(
                                icon: Icons.straighten,
                                label: '臂展',
                                value: '${r.specs.reachMm} mm',
                              ),
                            if (r.specs.dof != null)
                              _SpecChipWidget(
                                icon: Icons.rotate_90_degrees_ccw,
                                label: '自由度',
                                value: '${r.specs.dof} DOF',
                              ),
                          ],
                        ),
                        const SizedBox(height: 16),
                      ],

                      // 完整参数展开表格
                      if (specsMap.isNotEmpty)
                        ExpansionTile(
                          title: const Text('完整参数'),
                          initiallyExpanded: specsExpanded.value,
                          onExpansionChanged: (v) => specsExpanded.value = v,
                          tilePadding: EdgeInsets.zero,
                          childrenPadding: EdgeInsets.zero,
                          children: [
                            Card(
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(12),
                              ),
                              child: Column(
                                children: specsMap.entries.map((entry) {
                                  final isLast =
                                      entry.key == specsMap.keys.last;
                                  return Column(
                                    children: [
                                      Padding(
                                        padding: const EdgeInsets.symmetric(
                                          horizontal: 16,
                                          vertical: 10,
                                        ),
                                        child: Row(
                                          children: [
                                            Expanded(
                                              child: Text(
                                                entry.key,
                                                style: theme.textTheme.bodyMedium
                                                    ?.copyWith(
                                                  color: theme.colorScheme
                                                      .onSurfaceVariant,
                                                ),
                                              ),
                                            ),
                                            Text(
                                              entry.value,
                                              style: theme.textTheme.bodyMedium
                                                  ?.copyWith(
                                                fontWeight: FontWeight.w500,
                                              ),
                                            ),
                                          ],
                                        ),
                                      ),
                                      if (!isLast)
                                        Divider(
                                          height: 1,
                                          indent: 16,
                                          endIndent: 16,
                                          color: theme.colorScheme.outlineVariant,
                                        ),
                                    ],
                                  );
                                }).toList(),
                              ),
                            ),
                          ],
                        ),
                      const SizedBox(height: 16),

                      // 描述文本（可展开）
                      if (description.isNotEmpty) ...[
                        Text(
                          '产品简介',
                          style: theme.textTheme.titleSmall?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          descExpanded.value ? description : descShort,
                          style: theme.textTheme.bodyMedium?.copyWith(
                            color: theme.colorScheme.onSurfaceVariant,
                            height: 1.6,
                          ),
                        ),
                        if (description.length > 100)
                          GestureDetector(
                            onTap: () =>
                                descExpanded.value = !descExpanded.value,
                            child: Padding(
                              padding: const EdgeInsets.only(top: 4),
                              child: Text(
                                descExpanded.value ? '收起' : '展开',
                                style: TextStyle(
                                  color: theme.colorScheme.primary,
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                            ),
                          ),
                        const SizedBox(height: 20),
                      ],

                      // 询价入口
                      SizedBox(
                        width: double.infinity,
                        child: FilledButton.icon(
                          onPressed: () => _showInquirySheet(context, r, dioClient),
                          icon: const Icon(Icons.email_outlined),
                          label: const Text('询价 / 联系厂商'),
                          style: FilledButton.styleFrom(
                            backgroundColor: Colors.orange,
                            foregroundColor: Colors.white,
                            padding: const EdgeInsets.symmetric(vertical: 14),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(12),
                            ),
                          ),
                        ),
                      ),
                      const SizedBox(height: 20),

                      // 相关机器人
                      if (similarRobots.value.isNotEmpty) ...[
                        Text(
                          '相关机器人',
                          style: theme.textTheme.titleSmall?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 12),
                        SizedBox(
                          height: 180,
                          child: ListView.builder(
                            scrollDirection: Axis.horizontal,
                            itemCount: similarRobots.value.length,
                            itemBuilder: (context, index) {
                              final sr = similarRobots.value[index];
                              return SizedBox(
                                width: 140,
                                child: Padding(
                                  padding: EdgeInsets.only(
                                    right: index < similarRobots.value.length - 1 ? 12 : 0,
                                  ),
                                  child: RobotCard(
                                    robot: sr,
                                    onTap: () => context.push('/robots/${sr.id}'),
                                  ),
                                ),
                              );
                            },
                          ),
                        ),
                        const SizedBox(height: 20),
                      ],
                    ],
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  void _showInquirySheet(BuildContext context, Robot r, dynamic dioClient) {
    final formKey = GlobalKey<FormState>();
    final messageCtrl = TextEditingController();
    final nameCtrl = TextEditingController();
    final emailCtrl = TextEditingController();
    final phoneCtrl = TextEditingController();
    final companyCtrl = TextEditingController();
    bool submitting = false;
    bool success = false;
    String errorMsg = '';

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setState) => Padding(
          padding: EdgeInsets.only(
            left: 20, right: 20, top: 20,
            bottom: MediaQuery.of(ctx).viewInsets.bottom + 20,
          ),
          child: success
              ? Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(Icons.check_circle, color: Colors.green, size: 56),
                    const SizedBox(height: 12),
                    const Text('询价已提交！', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                    const SizedBox(height: 6),
                    const Text('厂商将尽快通过邮件与您联系。', style: TextStyle(color: Colors.grey)),
                    const SizedBox(height: 20),
                    FilledButton(
                      onPressed: () => Navigator.pop(ctx),
                      child: const Text('关闭'),
                    ),
                    const SizedBox(height: 12),
                  ],
                )
              : Form(
                  key: formKey,
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Expanded(
                            child: Text(
                              '询价 ${r.name}',
                              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                            ),
                          ),
                          IconButton(onPressed: () => Navigator.pop(ctx), icon: const Icon(Icons.close)),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Text('向 ${r.manufacturer.name} 发起询价', style: const TextStyle(color: Colors.grey, fontSize: 13)),
                      const SizedBox(height: 16),
                      TextFormField(
                        controller: messageCtrl,
                        maxLines: 4,
                        maxLength: 2000,
                        decoration: const InputDecoration(
                          labelText: '询价内容 *',
                          hintText: '请描述您的需求，例如：采购数量、应用场景...',
                          border: OutlineInputBorder(),
                        ),
                        validator: (v) => (v == null || v.length < 10) ? '内容不少于10字' : null,
                      ),
                      const SizedBox(height: 12),
                      Row(
                        children: [
                          Expanded(
                            child: TextFormField(
                              controller: nameCtrl,
                              decoration: const InputDecoration(labelText: '联系人', border: OutlineInputBorder()),
                            ),
                          ),
                          const SizedBox(width: 10),
                          Expanded(
                            child: TextFormField(
                              controller: emailCtrl,
                              keyboardType: TextInputType.emailAddress,
                              decoration: const InputDecoration(labelText: '邮箱 *', border: OutlineInputBorder()),
                              validator: (v) => (v == null || !v.contains('@')) ? '请输入有效邮箱' : null,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 12),
                      Row(
                        children: [
                          Expanded(
                            child: TextFormField(
                              controller: phoneCtrl,
                              keyboardType: TextInputType.phone,
                              decoration: const InputDecoration(labelText: '电话', border: OutlineInputBorder()),
                            ),
                          ),
                          const SizedBox(width: 10),
                          Expanded(
                            child: TextFormField(
                              controller: companyCtrl,
                              decoration: const InputDecoration(labelText: '公司', border: OutlineInputBorder()),
                            ),
                          ),
                        ],
                      ),
                      if (errorMsg.isNotEmpty) ...[
                        const SizedBox(height: 8),
                        Text(errorMsg, style: const TextStyle(color: Colors.red, fontSize: 13)),
                      ],
                      const SizedBox(height: 16),
                      SizedBox(
                        width: double.infinity,
                        child: FilledButton(
                          onPressed: submitting
                              ? null
                              : () async {
                                  if (!formKey.currentState!.validate()) return;
                                  setState(() { submitting = true; errorMsg = ''; });
                                  try {
                                    await dioClient.dio.post('/inquiries', data: {
                                      'robotId': r.id,
                                      'message': messageCtrl.text,
                                      'contactName': nameCtrl.text.isEmpty ? null : nameCtrl.text,
                                      'contactEmail': emailCtrl.text,
                                      'contactPhone': phoneCtrl.text.isEmpty ? null : phoneCtrl.text,
                                      'contactCompany': companyCtrl.text.isEmpty ? null : companyCtrl.text,
                                    });
                                    setState(() { success = true; });
                                  } catch (e) {
                                    setState(() { errorMsg = '提交失败，请稍后重试'; submitting = false; });
                                  }
                                },
                          style: FilledButton.styleFrom(
                            backgroundColor: Colors.orange,
                            padding: const EdgeInsets.symmetric(vertical: 14),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                          ),
                          child: submitting
                              ? const SizedBox(height: 18, width: 18, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                              : const Text('提交询价'),
                        ),
                      ),
                    ],
                  ),
                ),
        ),
      ),
    );
  }

  Widget _buildImagePlaceholder(ThemeData theme) {
    return Container(
      color: theme.colorScheme.surfaceContainerHighest,
      child: Center(
        child: Icon(
          Icons.smart_toy_outlined,
          size: 64,
          color: theme.colorScheme.onSurfaceVariant,
        ),
      ),
    );
  }
}

class _TabButton extends StatelessWidget {
  final String label;
  final bool selected;
  final VoidCallback onTap;

  const _TabButton({
    required this.label,
    required this.selected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
        decoration: BoxDecoration(
          color: selected ? Colors.white : Colors.transparent,
          borderRadius: BorderRadius.circular(18),
        ),
        child: Text(
          label,
          style: TextStyle(
            color: selected ? Colors.black87 : Colors.white,
            fontWeight: selected ? FontWeight.bold : FontWeight.normal,
            fontSize: 13,
          ),
        ),
      ),
    );
  }
}

class _SpecChipWidget extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;

  const _SpecChipWidget({
    required this.icon,
    required this.label,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(10),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 14, color: theme.colorScheme.primary),
          const SizedBox(width: 6),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                label,
                style: theme.textTheme.labelSmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              Text(
                value,
                style: theme.textTheme.labelMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
