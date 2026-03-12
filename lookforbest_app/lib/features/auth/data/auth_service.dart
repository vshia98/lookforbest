import 'package:dio/dio.dart';
import 'package:google_sign_in/google_sign_in.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_response.dart';
import '../../../core/storage/secure_storage.dart';
import '../domain/auth_models.dart';

/// 认证服务
class AuthService {
  final Dio _dio;
  final SecureStorage _storage;

  AuthService(this._dio, this._storage);

  final _googleSignIn = GoogleSignIn(scopes: ['email', 'profile']);

  /// 用户登录
  Future<AuthTokens> login({
    required String email,
    required String password,
  }) async {
    final response = await _dio.post(
      ApiConstants.login,
      data: {'email': email, 'password': password},
    );
    final apiResponse = ApiResponse.fromJson(
      response.data as Map<String, dynamic>,
      (data) => AuthTokens.fromJson(data as Map<String, dynamic>),
    );
    final tokens = apiResponse.data!;
    await _storage.saveTokens(
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken,
    );
    return tokens;
  }

  /// 用户注册
  Future<AuthTokens> register({
    required String email,
    required String password,
    required String username,
    String? displayName,
  }) async {
    final response = await _dio.post(
      ApiConstants.register,
      data: {
        'email': email,
        'password': password,
        'username': username,
        if (displayName != null) 'displayName': displayName,
      },
    );
    final apiResponse = ApiResponse.fromJson(
      response.data as Map<String, dynamic>,
      (data) => AuthTokens.fromJson(data as Map<String, dynamic>),
    );
    final tokens = apiResponse.data!;
    await _storage.saveTokens(
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken,
    );
    return tokens;
  }

  /// Google 一键登录
  Future<AuthTokens> loginWithGoogle() async {
    final googleUser = await _googleSignIn.signIn();
    if (googleUser == null) throw Exception('已取消 Google 登录');

    final googleAuth = await googleUser.authentication;
    final idToken = googleAuth.idToken;
    if (idToken == null) throw Exception('无法获取 Google idToken');

    return _oauthLogin(provider: 'google', code: idToken);
  }

  /// 社交账号登录（通用入口）
  Future<AuthTokens> _oauthLogin({
    required String provider,
    required String code,
  }) async {
    final response = await _dio.post(
      ApiConstants.oauthLogin,
      data: {'provider': provider, 'code': code},
    );
    final apiResponse = ApiResponse.fromJson(
      response.data as Map<String, dynamic>,
      (data) => AuthTokens.fromJson(data as Map<String, dynamic>),
    );
    final tokens = apiResponse.data!;
    await _storage.saveTokens(
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken,
    );
    return tokens;
  }

  /// 获取当前用户信息
  Future<User> getCurrentUser() async {
    final response = await _dio.get(ApiConstants.usersMe);
    final apiResponse = ApiResponse.fromJson(
      response.data as Map<String, dynamic>,
      (data) => User.fromJson(data as Map<String, dynamic>),
    );
    return apiResponse.data!;
  }

  /// 退出登录
  Future<void> logout() async {
    await _googleSignIn.signOut().catchError((_) {});
    await _storage.clearTokens();
  }

  Future<bool> isLoggedIn() => _storage.isLoggedIn();
}
