import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:go_router/go_router.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import '../../../core/constants/api_constants.dart';
import '../../../features/robots/domain/robot.dart';
import '../../../shared/providers/providers.dart';
import '../../../shared/widgets/robot_card.dart';

class FavoritesPage extends HookConsumerWidget {
  const FavoritesPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isLoggedIn = useState<bool?>(null);
    final favorites = useState<List<RobotSummary>>([]);
    final isLoading = useState(false);
    final error = useState<String?>(null);
    final refreshKey = useState(0);

    // 检查登录状态
    useEffect(() {
      ref.read(secureStorageServiceProvider).isLoggedIn().then((loggedIn) {
        isLoggedIn.value = loggedIn;
      });
      return null;
    }, []);

    // 登录后加载收藏列表
    useEffect(() {
      if (isLoggedIn.value != true) return null;

      isLoading.value = true;
      error.value = null;

      final dio = ref.read(dioClientProvider).dio;
      dio.get(ApiConstants.favorites).then((response) {
        final content = response.data['data']['content'] as List<dynamic>;
        favorites.value = content
            .map((e) => RobotSummary.fromJson(e as Map<String, dynamic>))
            .toList();
        isLoading.value = false;
      }).catchError((e) {
        error.value = e.toString();
        isLoading.value = false;
      });

      return null;
    }, [isLoggedIn.value, refreshKey.value]);

    return Scaffold(
      appBar: AppBar(title: const Text('我的收藏')),
      body: _buildBody(
        context,
        ref,
        isLoggedIn.value,
        favorites,
        isLoading.value,
        error.value,
        onRefresh: () async {
          refreshKey.value++;
        },
        onRemove: (robotId) async {
          try {
            final dio = ref.read(dioClientProvider).dio;
            await dio.delete('${ApiConstants.favorites}/$robotId');
            favorites.value =
                favorites.value.where((r) => r.id != robotId).toList();
          } catch (e) {
            if (context.mounted) {
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(content: Text('取消收藏失败：$e')),
              );
            }
          }
        },
      ),
    );
  }

  Widget _buildBody(
    BuildContext context,
    WidgetRef ref,
    bool? isLoggedIn,
    ValueNotifier<List<RobotSummary>> favorites,
    bool isLoading,
    String? error, {
    required Future<void> Function() onRefresh,
    required Future<void> Function(int robotId) onRemove,
  }) {
    // 登录状态未知：加载中
    if (isLoggedIn == null) {
      return const Center(child: CircularProgressIndicator());
    }

    // 未登录
    if (!isLoggedIn) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              Icons.favorite_border,
              size: 64,
              color: Theme.of(context).colorScheme.onSurfaceVariant,
            ),
            const SizedBox(height: 16),
            Text(
              '请先登录查看收藏',
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                  ),
            ),
            const SizedBox(height: 24),
            FilledButton.icon(
              onPressed: () => context.push('/auth/login'),
              icon: const Icon(Icons.login),
              label: const Text('去登录'),
            ),
          ],
        ),
      );
    }

    // 加载中
    if (isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    // 错误
    if (error != null) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.error_outline, size: 48, color: Colors.red),
            const SizedBox(height: 12),
            Text('加载失败：$error'),
            const SizedBox(height: 16),
            FilledButton(
              onPressed: onRefresh,
              child: const Text('重试'),
            ),
          ],
        ),
      );
    }

    // 空收藏
    if (favorites.value.isEmpty) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              Icons.favorite_border,
              size: 64,
              color: Theme.of(context).colorScheme.onSurfaceVariant,
            ),
            const SizedBox(height: 16),
            Text(
              '还没有收藏的机器人',
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                  ),
            ),
            const SizedBox(height: 24),
            FilledButton.icon(
              onPressed: () => context.go('/robots'),
              icon: const Icon(Icons.search),
              label: const Text('去浏览'),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: onRefresh,
      child: ListView.builder(
        padding: const EdgeInsets.all(12),
        itemCount: favorites.value.length,
        itemBuilder: (context, index) {
          final robot = favorites.value[index];
          return _DismissibleRobotCard(
            key: ValueKey(robot.id),
            robot: robot,
            onRemove: () => onRemove(robot.id),
          );
        },
      ),
    );
  }
}

class _DismissibleRobotCard extends StatelessWidget {
  final RobotSummary robot;
  final VoidCallback onRemove;

  const _DismissibleRobotCard({
    super.key,
    required this.robot,
    required this.onRemove,
  });

  @override
  Widget build(BuildContext context) {
    return Dismissible(
      key: ValueKey(robot.id),
      direction: DismissDirection.endToStart,
      background: Container(
        alignment: Alignment.centerRight,
        padding: const EdgeInsets.symmetric(horizontal: 24),
        margin: const EdgeInsets.only(bottom: 12),
        decoration: BoxDecoration(
          color: Colors.red,
          borderRadius: BorderRadius.circular(12),
        ),
        child: const Icon(Icons.delete_outline, color: Colors.white, size: 28),
      ),
      confirmDismiss: (_) async {
        return await showDialog<bool>(
          context: context,
          builder: (ctx) => AlertDialog(
            title: const Text('取消收藏'),
            content: Text('确定要取消收藏「${robot.name}」吗？'),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(ctx, false),
                child: const Text('取消'),
              ),
              FilledButton(
                onPressed: () => Navigator.pop(ctx, true),
                style: FilledButton.styleFrom(
                  backgroundColor: Colors.red,
                ),
                child: const Text('确定'),
              ),
            ],
          ),
        );
      },
      onDismissed: (_) => onRemove(),
      child: GestureDetector(
        onLongPress: () async {
          final confirmed = await showDialog<bool>(
            context: context,
            builder: (ctx) => AlertDialog(
              title: const Text('取消收藏'),
              content: Text('确定要取消收藏「${robot.name}」吗？'),
              actions: [
                TextButton(
                  onPressed: () => Navigator.pop(ctx, false),
                  child: const Text('取消'),
                ),
                FilledButton(
                  onPressed: () => Navigator.pop(ctx, true),
                  style: FilledButton.styleFrom(
                    backgroundColor: Colors.red,
                  ),
                  child: const Text('确定'),
                ),
              ],
            ),
          );
          if (confirmed == true) onRemove();
        },
        child: Padding(
          padding: const EdgeInsets.only(bottom: 12),
          child: RobotCard(
            robot: robot,
            onTap: () => context.push('/robots/${robot.id}'),
          ),
        ),
      ),
    );
  }
}
