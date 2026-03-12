/// 用户模型
class User {
  final int id;
  final String email;
  final String username;
  final String? displayName;
  final String role;

  const User({
    required this.id,
    required this.email,
    required this.username,
    this.displayName,
    required this.role,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'] as int,
      email: json['email'] as String,
      username: json['username'] as String,
      displayName: json['displayName'] as String?,
      role: json['role'] as String? ?? 'user',
    );
  }
}

/// 登录响应
class AuthTokens {
  final String accessToken;
  final String refreshToken;
  final int expiresIn;
  final User user;

  const AuthTokens({
    required this.accessToken,
    required this.refreshToken,
    required this.expiresIn,
    required this.user,
  });

  factory AuthTokens.fromJson(Map<String, dynamic> json) {
    return AuthTokens(
      accessToken: json['accessToken'] as String,
      refreshToken: json['refreshToken'] as String,
      expiresIn: json['expiresIn'] as int,
      user: User.fromJson(json['user'] as Map<String, dynamic>),
    );
  }
}
