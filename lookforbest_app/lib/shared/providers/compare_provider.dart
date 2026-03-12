import 'package:flutter_riverpod/flutter_riverpod.dart';

/// 全局对比列表 Provider（存储机器人 ID 列表，最多4个）
final compareIdsProvider = StateProvider<List<int>>((ref) => []);

/// 切换对比状态（添加/移除）
extension CompareNotifier on StateController<List<int>> {
  void toggle(int robotId) {
    if (state.contains(robotId)) {
      state = state.where((id) => id != robotId).toList();
    } else if (state.length < 4) {
      state = [...state, robotId];
    }
  }
}
