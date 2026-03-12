import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// JWT Token 安全存储封装
class SecureStorage {
  final FlutterSecureStorage _storage;

  SecureStorage(this._storage);

  Future<void> saveTokens({
    required String accessToken,
    required String refreshToken,
  }) async {
    await Future.wait([
      _storage.write(key: 'access_token', value: accessToken),
      _storage.write(key: 'refresh_token', value: refreshToken),
    ]);
  }

  Future<String?> getAccessToken() => _storage.read(key: 'access_token');
  Future<String?> getRefreshToken() => _storage.read(key: 'refresh_token');

  Future<void> clearTokens() => _storage.deleteAll();

  Future<bool> isLoggedIn() async {
    final token = await getAccessToken();
    return token != null;
  }
}
