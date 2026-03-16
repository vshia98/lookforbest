import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:go_router/go_router.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../../shared/providers/providers.dart';
import '../../../shared/widgets/error_view.dart';
import '../../../shared/widgets/loading_indicator.dart';
import '../../../shared/widgets/robot_card.dart';
import '../../../shared/widgets/theme_toggle_button.dart';
import '../domain/robot.dart';

class RobotListPage extends HookConsumerWidget {
  const RobotListPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final robotService = ref.read(robotServiceProvider);

    final robots = useState<List<RobotSummary>>([]);
    final loading = useState(true);
    final loadingMore = useState(false);
    final error = useState<String?>(null);
    final hasMore = useState(true);
    final filter = useState(const RobotFilter());

    final categories = useState<List<RobotCategory>>([]);

    final scrollController = useScrollController();
    final searchController = useTextEditingController();
    final debounceTimer = useRef<Timer?>(null);

    Future<void> fetchRobots({bool reset = false}) async {
      if (reset) {
        loading.value = true;
        error.value = null;
      }
      try {
        final currentFilter = reset
            ? filter.value.copyWith(page: 0)
            : filter.value;
        final result = await robotService.getRobots(currentFilter);
        if (reset) {
          robots.value = result.content;
        } else {
          robots.value = [...robots.value, ...result.content];
        }
        hasMore.value = result.hasNext;
      } catch (e) {
        error.value = e.toString();
      } finally {
        loading.value = false;
        loadingMore.value = false;
      }
    }

    Future<void> loadMore() async {
      if (loadingMore.value || !hasMore.value) return;
      loadingMore.value = true;
      filter.value = filter.value.copyWith(page: filter.value.page + 1);
      try {
        final result = await robotService.getRobots(filter.value);
        robots.value = [...robots.value, ...result.content];
        hasMore.value = result.hasNext;
      } catch (_) {
        // 加载更多失败时静默处理，回退页码
        filter.value = filter.value.copyWith(page: filter.value.page - 1);
      } finally {
        loadingMore.value = false;
      }
    }

    Future<void> loadCategories() async {
      try {
        categories.value = await robotService.getCategories();
      } catch (_) {}
    }

    useEffect(() {
      fetchRobots(reset: true);
      loadCategories();
      return null;
    }, const []);

    // 监听滚动到底部
    useEffect(() {
      void listener() {
        if (scrollController.position.pixels >=
            scrollController.position.maxScrollExtent - 200) {
          loadMore();
        }
      }

      scrollController.addListener(listener);
      return () => scrollController.removeListener(listener);
    }, [scrollController]);

    void onSearchChanged(String value) {
      debounceTimer.value?.cancel();
      debounceTimer.value = Timer(const Duration(milliseconds: 500), () {
        filter.value = filter.value.copyWith(q: value, page: 0);
        robots.value = [];
        fetchRobots(reset: true);
      });
    }

    void showFilterSheet() {
      showModalBottomSheet(
        context: context,
        isScrollControlled: true,
        shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
        ),
        builder: (ctx) => _FilterSheet(
          currentFilter: filter.value,
          categories: categories.value,
          onApply: (newFilter) {
            filter.value = newFilter.copyWith(page: 0);
            robots.value = [];
            Navigator.of(ctx).pop();
            fetchRobots(reset: true);
          },
        ),
      );
    }

    Widget buildBody() {
      if (loading.value && robots.value.isEmpty) {
        return const LoadingIndicator(text: '加载中...');
      }

      if (error.value != null && robots.value.isEmpty) {
        return ErrorView(
          message: error.value!,
          onRetry: () => fetchRobots(reset: true),
        );
      }

      return RefreshIndicator(
        onRefresh: () async {
          filter.value = filter.value.copyWith(page: 0);
          robots.value = [];
          await fetchRobots(reset: true);
        },
        child: CustomScrollView(
          controller: scrollController,
          slivers: [
            SliverPadding(
              padding: const EdgeInsets.all(12),
              sliver: SliverGrid(
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 2,
                  crossAxisSpacing: 12,
                  mainAxisSpacing: 12,
                  childAspectRatio: 0.72,
                ),
                delegate: SliverChildBuilderDelegate(
                  (context, index) {
                    final robot = robots.value[index];
                    return RobotCard(
                      robot: robot,
                      onTap: () => context.push('/robots/${robot.id}'),
                    );
                  },
                  childCount: robots.value.length,
                ),
              ),
            ),
            if (loadingMore.value)
              const SliverToBoxAdapter(
                child: Padding(
                  padding: EdgeInsets.symmetric(vertical: 16),
                  child: Center(child: CircularProgressIndicator()),
                ),
              ),
            if (!hasMore.value && robots.value.isNotEmpty)
              const SliverToBoxAdapter(
                child: Padding(
                  padding: EdgeInsets.symmetric(vertical: 16),
                  child: Center(
                    child: Text(
                      '已加载全部',
                      style: TextStyle(color: Colors.grey),
                    ),
                  ),
                ),
              ),
          ],
        ),
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text('机器人库'),
        actions: [
          const ThemeToggleButton(),
          IconButton(
            icon: const Icon(Icons.person_outline),
            onPressed: () => context.push('/auth/login'),
          ),
        ],
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(56),
          child: Padding(
            padding: const EdgeInsets.fromLTRB(12, 0, 12, 8),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: searchController,
                    onChanged: onSearchChanged,
                    decoration: InputDecoration(
                      hintText: '搜索机器人...',
                      prefixIcon: const Icon(Icons.search, size: 20),
                      contentPadding: const EdgeInsets.symmetric(vertical: 8),
                      isDense: true,
                      filled: true,
                      fillColor: Theme.of(context).colorScheme.surfaceContainerHighest,
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(24),
                        borderSide: BorderSide.none,
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                IconButton.filled(
                  icon: const Icon(Icons.tune),
                  onPressed: showFilterSheet,
                  style: IconButton.styleFrom(
                    backgroundColor: Theme.of(context).colorScheme.primaryContainer,
                    foregroundColor: Theme.of(context).colorScheme.onPrimaryContainer,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
      body: buildBody(),
    );
  }
}

class _FilterSheet extends HookWidget {
  final RobotFilter currentFilter;
  final List<RobotCategory> categories;
  final void Function(RobotFilter) onApply;

  const _FilterSheet({
    required this.currentFilter,
    required this.categories,
    required this.onApply,
  });

  @override
  Widget build(BuildContext context) {
    final selectedCategoryId = useState<int?>(currentFilter.categoryId);
    final selectedSort = useState(currentFilter.sort);

    const sortOptions = [
      ('relevance', '综合'),
      ('latest', '最新'),
      ('popular', '最受欢迎'),
    ];

    return Padding(
      padding: EdgeInsets.only(
        left: 20,
        right: 20,
        top: 20,
        bottom: MediaQuery.of(context).viewInsets.bottom + 20,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                '筛选',
                style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
              ),
              TextButton(
                onPressed: () {
                  selectedCategoryId.value = null;
                  selectedSort.value = 'relevance';
                },
                child: const Text('重置'),
              ),
            ],
          ),
          const SizedBox(height: 16),

          // 分类选择
          Text('分类', style: Theme.of(context).textTheme.labelLarge),
          const SizedBox(height: 8),
          DropdownButtonFormField<int?>(
            initialValue: selectedCategoryId.value,
            decoration: InputDecoration(
              filled: true,
              fillColor: Theme.of(context).colorScheme.surfaceContainerHighest,
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide.none,
              ),
              contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            ),
            hint: const Text('全部分类'),
            items: [
              const DropdownMenuItem<int?>(
                value: null,
                child: Text('全部分类'),
              ),
              ...categories.map(
                (c) => DropdownMenuItem<int?>(
                  value: c.id,
                  child: Text(c.name),
                ),
              ),
            ],
            onChanged: (v) => selectedCategoryId.value = v,
          ),
          const SizedBox(height: 20),

          // 排序方式
          Text('排序方式', style: Theme.of(context).textTheme.labelLarge),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            children: sortOptions.map((opt) {
              final isSelected = selectedSort.value == opt.$1;
              return ChoiceChip(
                label: Text(opt.$2),
                selected: isSelected,
                onSelected: (_) => selectedSort.value = opt.$1,
              );
            }).toList(),
          ),
          const SizedBox(height: 24),

          SizedBox(
            width: double.infinity,
            child: FilledButton(
              onPressed: () {
                onApply(
                  currentFilter.copyWith(
                    categoryId: selectedCategoryId.value,
                    sort: selectedSort.value,
                  ),
                );
              },
              child: const Text('应用筛选'),
            ),
          ),
        ],
      ),
    );
  }
}
