import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'features/robots/presentation/robot_list_page.dart';
import 'features/robots/presentation/robot_detail_page.dart';
import 'features/compare/presentation/compare_page.dart';
import 'features/auth/presentation/login_page.dart';
import 'features/auth/presentation/register_page.dart';
import 'features/favorites/presentation/favorites_page.dart';
import 'shared/providers/theme_provider.dart';

/// 路由配置
final routerProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: '/robots',
    routes: [
      GoRoute(
        path: '/',
        redirect: (_, __) => '/robots',
      ),
      GoRoute(
        path: '/robots',
        name: 'robots',
        builder: (context, state) => const RobotListPage(),
      ),
      GoRoute(
        path: '/robots/:id',
        name: 'robot-detail',
        builder: (context, state) {
          final id = state.pathParameters['id']!;
          return RobotDetailPage(id: id);
        },
      ),
      GoRoute(
        path: '/compare',
        name: 'compare',
        builder: (context, state) => const ComparePage(),
      ),
      GoRoute(
        path: '/auth/login',
        name: 'login',
        builder: (context, state) => const LoginPage(),
      ),
      GoRoute(
        path: '/auth/register',
        name: 'register',
        builder: (context, state) => const RegisterPage(),
      ),
      GoRoute(
        path: '/profile/favorites',
        name: 'favorites',
        builder: (context, state) => const FavoritesPage(),
      ),
    ],
  );
});

/// LookForBest App 根组件
class LookForBestApp extends ConsumerWidget {
  const LookForBestApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(routerProvider);
    final themeMode = ref.watch(themeModeProvider);
    return MaterialApp.router(
      title: 'LookForBest',
      debugShowCheckedModeBanner: false,
      themeMode: themeMode,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF6366F1), // Indigo
          brightness: Brightness.light,
        ),
        useMaterial3: true,
      ),
      darkTheme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF6366F1),
          brightness: Brightness.dark,
        ),
        useMaterial3: true,
      ),
      routerConfig: router,
    );
  }
}
