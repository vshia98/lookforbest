/// 统一 API 响应模型
/// 对应后端格式: { success, code, message, data, timestamp }
class ApiResponse<T> {
  final bool success;
  final int code;
  final String message;
  final T? data;
  final int? timestamp;

  const ApiResponse({
    required this.success,
    required this.code,
    required this.message,
    this.data,
    this.timestamp,
  });

  factory ApiResponse.fromJson(
    Map<String, dynamic> json,
    T Function(dynamic) fromJsonT,
  ) {
    return ApiResponse<T>(
      success: json['success'] as bool,
      code: json['code'] as int,
      message: json['message'] as String,
      data: json['data'] != null ? fromJsonT(json['data']) : null,
      timestamp: json['timestamp'] as int?,
    );
  }
}

/// 分页响应
class PagedData<T> {
  final List<T> content;
  final int page;
  final int size;
  final int total;
  final int totalPages;
  final bool hasNext;

  const PagedData({
    required this.content,
    required this.page,
    required this.size,
    required this.total,
    required this.totalPages,
    required this.hasNext,
  });

  factory PagedData.fromJson(
    Map<String, dynamic> json,
    T Function(Map<String, dynamic>) fromJsonT,
  ) {
    return PagedData<T>(
      content: (json['content'] as List)
          .map((e) => fromJsonT(e as Map<String, dynamic>))
          .toList(),
      page: json['page'] as int,
      size: json['size'] as int,
      total: json['total'] as int,
      totalPages: json['totalPages'] as int,
      hasNext: json['hasNext'] as bool,
    );
  }
}
