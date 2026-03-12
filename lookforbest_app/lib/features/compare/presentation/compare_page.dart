import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:go_router/go_router.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import '../../../features/robots/domain/robot.dart';
import '../../../shared/providers/providers.dart';

class ComparePage extends HookConsumerWidget {
  const ComparePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final compareIds = ref.watch(compareIdsProvider);
    final robots = useState<List<Robot?>>([]);
    final isLoading = useState(false);
    final error = useState<String?>(null);

    useEffect(() {
      if (compareIds.isEmpty) {
        robots.value = [];
        return null;
      }

      isLoading.value = true;
      error.value = null;

      final robotService = ref.read(robotServiceProvider);
      Future.wait(
        compareIds.map((id) => robotService.getRobotById(id).then<Robot?>((r) => r).catchError((_) => null as Robot?)),
      ).then((results) {
        robots.value = results;
        isLoading.value = false;
      }).catchError((e) {
        error.value = e.toString();
        isLoading.value = false;
      });

      return null;
    }, [compareIds]);

    return Scaffold(
      appBar: AppBar(
        title: const Text('机器人对比'),
        actions: [
          if (compareIds.isNotEmpty)
            TextButton.icon(
              onPressed: () {
                ref.read(compareIdsProvider.notifier).state = [];
              },
              icon: const Icon(Icons.clear_all, size: 18),
              label: const Text('清空'),
            ),
        ],
      ),
      body: _buildBody(context, ref, compareIds, robots.value, isLoading.value, error.value),
    );
  }

  Widget _buildBody(
    BuildContext context,
    WidgetRef ref,
    List<int> compareIds,
    List<Robot?> robots,
    bool isLoading,
    String? error,
  ) {
    if (compareIds.isEmpty) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              Icons.compare_arrows,
              size: 64,
              color: Theme.of(context).colorScheme.onSurfaceVariant,
            ),
            const SizedBox(height: 16),
            Text(
              '从机器人列表添加对比项',
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                  ),
            ),
            const SizedBox(height: 24),
            FilledButton.icon(
              onPressed: () => context.go('/robots'),
              icon: const Icon(Icons.search),
              label: const Text('去选择'),
            ),
          ],
        ),
      );
    }

    if (isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (error != null) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.error_outline, size: 48, color: Colors.red),
            const SizedBox(height: 12),
            Text('加载失败：$error'),
          ],
        ),
      );
    }

    final validRobots = robots.whereType<Robot>().toList();
    if (validRobots.isEmpty) {
      return const Center(child: Text('暂无对比数据'));
    }

    return _CompareTable(robots: validRobots, ref: ref);
  }
}

class _CompareTable extends StatelessWidget {
  final List<Robot> robots;
  final WidgetRef ref;

  const _CompareTable({required this.robots, required this.ref});

  static const _params = [
    _Param('载荷 (kg)', _ParamKey.payload),
    _Param('臂展 (mm)', _ParamKey.reach),
    _Param('自由度', _ParamKey.dof),
    _Param('重复精度 (mm)', _ParamKey.repeatability),
    _Param('最大速度 (°/s)', _ParamKey.maxSpeed),
    _Param('自重 (kg)', _ParamKey.weight),
    _Param('IP 等级', _ParamKey.ipRating),
    _Param('安装方式', _ParamKey.mounting),
    _Param('分类', _ParamKey.category),
    _Param('厂商', _ParamKey.manufacturer),
  ];

  String _getValue(Robot robot, _ParamKey key) {
    switch (key) {
      case _ParamKey.payload:
        return robot.specs.payloadKg != null
            ? robot.specs.payloadKg!.toStringAsFixed(1)
            : '-';
      case _ParamKey.reach:
        return robot.specs.reachMm?.toString() ?? '-';
      case _ParamKey.dof:
        return robot.specs.dof?.toString() ?? '-';
      case _ParamKey.repeatability:
        return robot.specs.repeatabilityMm != null
            ? robot.specs.repeatabilityMm!.toStringAsFixed(2)
            : '-';
      case _ParamKey.maxSpeed:
        return robot.specs.maxSpeedDegS != null
            ? robot.specs.maxSpeedDegS!.toStringAsFixed(1)
            : '-';
      case _ParamKey.weight:
        return robot.specs.weightKg != null
            ? robot.specs.weightKg!.toStringAsFixed(1)
            : '-';
      case _ParamKey.ipRating:
        return robot.specs.ipRating ?? '-';
      case _ParamKey.mounting:
        return robot.specs.mounting ?? '-';
      case _ParamKey.category:
        return robot.category.name;
      case _ParamKey.manufacturer:
        return robot.manufacturer.name;
    }
  }

  bool _isDifferent(_ParamKey key) {
    if (robots.length < 2) return false;
    final values = robots.map((r) => _getValue(r, key)).toSet();
    // 只有当所有机器人都有值（不含'-'）且值不同时才标记差异
    final allHaveValues = robots.every((r) => _getValue(r, key) != '-');
    return allHaveValues && values.length > 1;
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    const labelColWidth = 110.0;
    const dataColWidth = 160.0;

    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      child: SingleChildScrollView(
        scrollDirection: Axis.vertical,
        child: IntrinsicHeight(
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // 参数名列（固定）
              SizedBox(
                width: labelColWidth,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // 顶部占位（对应机器人信息区域高度）
                    const SizedBox(height: 160),
                    // 分割线
                    Divider(height: 1, color: theme.dividerColor),
                    // 参数标签
                    ..._params.map((p) {
                      final diff = _isDifferent(p.key);
                      return _LabelCell(label: p.label, highlighted: diff);
                    }),
                  ],
                ),
              ),
              // 各机器人数据列
              ...robots.map((robot) {
                return SizedBox(
                  width: dataColWidth,
                  child: Column(
                    children: [
                      // 机器人封面 + 名称 + 移除按钮
                      Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 12),
                        child: Column(
                          children: [
                            ClipRRect(
                              borderRadius: BorderRadius.circular(8),
                              child: robot.coverImageUrl != null
                                  ? CachedNetworkImage(
                                      imageUrl: robot.coverImageUrl!,
                                      width: 80,
                                      height: 80,
                                      fit: BoxFit.cover,
                                      placeholder: (_, __) => Container(
                                        width: 80,
                                        height: 80,
                                        color: theme.colorScheme.surfaceContainerHighest,
                                      ),
                                      errorWidget: (_, __, ___) => _robotPlaceholder(theme),
                                    )
                                  : _robotPlaceholder(theme),
                            ),
                            const SizedBox(height: 6),
                            Text(
                              robot.name,
                              style: theme.textTheme.labelMedium?.copyWith(
                                fontWeight: FontWeight.bold,
                              ),
                              textAlign: TextAlign.center,
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                            ),
                            const SizedBox(height: 4),
                            TextButton(
                              onPressed: () {
                                final notifier = ref.read(compareIdsProvider.notifier);
                                notifier.state = notifier.state
                                    .where((id) => id != robot.id)
                                    .toList();
                              },
                              style: TextButton.styleFrom(
                                foregroundColor: theme.colorScheme.error,
                                padding: EdgeInsets.zero,
                                minimumSize: const Size(0, 28),
                              ),
                              child: const Text('移除', style: TextStyle(fontSize: 12)),
                            ),
                          ],
                        ),
                      ),
                      Divider(height: 1, color: theme.dividerColor),
                      // 参数值
                      ..._params.map((p) {
                        final diff = _isDifferent(p.key);
                        final value = _getValue(robot, p.key);
                        return _DataCell(value: value, highlighted: diff);
                      }),
                    ],
                  ),
                );
              }),
            ],
          ),
        ),
      ),
    );
  }

  Widget _robotPlaceholder(ThemeData theme) {
    return Container(
      width: 80,
      height: 80,
      color: theme.colorScheme.surfaceContainerHighest,
      child: Icon(
        Icons.precision_manufacturing,
        size: 36,
        color: theme.colorScheme.onSurfaceVariant,
      ),
    );
  }
}

class _LabelCell extends StatelessWidget {
  final String label;
  final bool highlighted;

  const _LabelCell({required this.label, required this.highlighted});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      height: 48,
      padding: const EdgeInsets.symmetric(horizontal: 8),
      decoration: BoxDecoration(
        color: highlighted
            ? Colors.amber.withValues(alpha: 0.15)
            : Colors.transparent,
        border: Border(
          bottom: BorderSide(color: theme.dividerColor, width: 0.5),
        ),
      ),
      alignment: Alignment.centerLeft,
      child: Text(
        label,
        style: theme.textTheme.bodySmall?.copyWith(
          color: theme.colorScheme.onSurfaceVariant,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }
}

class _DataCell extends StatelessWidget {
  final String value;
  final bool highlighted;

  const _DataCell({required this.value, required this.highlighted});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      height: 48,
      padding: const EdgeInsets.symmetric(horizontal: 8),
      decoration: BoxDecoration(
        color: highlighted
            ? Colors.amber.withValues(alpha: 0.12)
            : Colors.transparent,
        border: Border(
          bottom: BorderSide(color: theme.dividerColor, width: 0.5),
          left: BorderSide(color: theme.dividerColor, width: 0.5),
        ),
      ),
      alignment: Alignment.center,
      child: Text(
        value,
        style: theme.textTheme.bodyMedium?.copyWith(
          fontWeight: highlighted ? FontWeight.bold : FontWeight.normal,
        ),
        textAlign: TextAlign.center,
      ),
    );
  }
}

enum _ParamKey {
  payload,
  reach,
  dof,
  repeatability,
  maxSpeed,
  weight,
  ipRating,
  mounting,
  category,
  manufacturer,
}

class _Param {
  final String label;
  final _ParamKey key;

  const _Param(this.label, this.key);
}
