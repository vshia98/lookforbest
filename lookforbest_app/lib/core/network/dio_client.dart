import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../constants/api_constants.dart';


/// Dio HTTP 客户端，带 JWT 自动刷新拦截器
class DioClient {
  static DioClient? _instance;
  late final Dio _dio;
  final FlutterSecureStorage _storage;

  DioClient._({required FlutterSecureStorage storage}) : _storage = storage {
    _dio = Dio(
      BaseOptions(
        baseUrl: ApiConstants.baseUrl,
        connectTimeout: ApiConstants.connectTimeout,
        receiveTimeout: ApiConstants.receiveTimeout,
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
      ),
    );
    _dio.interceptors.addAll([
      _AuthInterceptor(storage: _storage, dio: _dio),
      LogInterceptor(
        requestBody: true,
        responseBody: true,
        logPrint: (obj) => print('[DioClient] $obj'),
      ),
    ]);
  }

  factory DioClient({required FlutterSecureStorage storage}) {
    _instance ??= DioClient._(storage: storage);
    return _instance!;
  }

  Dio get dio => _dio;
}

/// JWT 认证拦截器：自动附加 token，401 时自动刷新
class _AuthInterceptor extends Interceptor {
  final FlutterSecureStorage _storage;
  final Dio _dio;
  bool _isRefreshing = false;

  _AuthInterceptor({required FlutterSecureStorage storage, required Dio dio})
      : _storage = storage,
        _dio = dio;

  @override
  void onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final token = await _storage.read(key: 'access_token');
    if (token != null) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    if (err.response?.statusCode == 401 && !_isRefreshing) {
      _isRefreshing = true;
      try {
        final refreshToken = await _storage.read(key: 'refresh_token');
        if (refreshToken != null) {
          final response = await _dio.post(
            ApiConstants.refresh,
            data: {'refreshToken': refreshToken},
          );
          final newToken = response.data['data']['accessToken'] as String;
          await _storage.write(key: 'access_token', value: newToken);
          // 使用新 token 重试原请求
          err.requestOptions.headers['Authorization'] = 'Bearer $newToken';
          final retryResponse = await _dio.fetch(err.requestOptions);
          handler.resolve(retryResponse);
          return;
        }
      } catch (_) {
        await _storage.deleteAll(); // 清除过期 token，触发重新登录
      } finally {
        _isRefreshing = false;
      }
    }
    handler.next(err);
  }
}
