import 'package:dio/dio.dart';

/// 统一应用异常
class AppException implements Exception {
  final String message;
  final int? code;

  const AppException({required this.message, this.code});

  factory AppException.fromDioException(DioException e) {
    switch (e.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
        return const AppException(message: '网络连接超时，请检查网络');
      case DioExceptionType.badResponse:
        final statusCode = e.response?.statusCode;
        final msg = e.response?.data?['message'] as String? ?? '请求失败';
        return AppException(message: msg, code: statusCode);
      case DioExceptionType.connectionError:
        return const AppException(message: '无法连接到服务器，请检查网络');
      default:
        return AppException(message: e.message ?? '未知错误');
    }
  }

  @override
  String toString() => 'AppException(code: $code, message: $message)';
}
