import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_response.dart';
import '../domain/robot.dart';

/// 机器人 API 服务层
class RobotService {
  final Dio _dio;

  RobotService(this._dio);

  /// 获取机器人列表（支持搜索+多维筛选+分页）
  Future<PagedData<RobotSummary>> getRobots(RobotFilter filter) async {
    final response = await _dio.get(
      ApiConstants.robots,
      queryParameters: filter.toQueryParams(),
    );
    final apiResponse = ApiResponse.fromJson(
      response.data as Map<String, dynamic>,
      (data) => PagedData.fromJson(
        data as Map<String, dynamic>,
        RobotSummary.fromJson,
      ),
    );
    return apiResponse.data!;
  }

  /// 获取机器人详情
  Future<Robot> getRobotById(int id) async {
    final response = await _dio.get('${ApiConstants.robots}/$id');
    final apiResponse = ApiResponse.fromJson(
      response.data as Map<String, dynamic>,
      (data) => Robot.fromJson(data as Map<String, dynamic>),
    );
    return apiResponse.data!;
  }

  /// 获取相似机器人
  Future<List<RobotSummary>> getSimilarRobots(int id, {int size = 6}) async {
    final response = await _dio.get(
      '${ApiConstants.robots}/$id/similar',
      queryParameters: {'size': size},
    );
    final apiResponse = ApiResponse.fromJson(
      response.data as Map<String, dynamic>,
      (data) => (data as List)
          .map((e) => RobotSummary.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
    return apiResponse.data!;
  }

  /// 获取分类列表
  Future<List<RobotCategory>> getCategories() async {
    final response = await _dio.get(ApiConstants.categories);
    final apiResponse = ApiResponse.fromJson(
      response.data as Map<String, dynamic>,
      (data) => (data as List)
          .map((e) => RobotCategory.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
    return apiResponse.data!;
  }

  /// 机器人对比
  Future<Map<String, dynamic>> compareRobots(List<int> robotIds) async {
    final response = await _dio.post(
      ApiConstants.compare,
      data: {'robotIds': robotIds},
    );
    return response.data['data'] as Map<String, dynamic>;
  }

  /// 搜索建议
  Future<List<Map<String, dynamic>>> searchSuggest(String q) async {
    if (q.isEmpty) return [];
    final response = await _dio.get(
      ApiConstants.searchSuggest,
      queryParameters: {'q': q, 'size': 8},
    );
    return (response.data['data']['suggestions'] as List)
        .cast<Map<String, dynamic>>();
  }
}
