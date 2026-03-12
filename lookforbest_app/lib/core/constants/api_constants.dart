/// API 常量配置
class ApiConstants {
  ApiConstants._();

  /// 通过 --dart-define=API_BASE_URL=xxx 注入生产地址
  /// 调试模式默认指向模拟器本地服务；release 构建需传入生产 URL
  static const String baseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'https://api.lookforbest.com/api/v1',
  );
  static const String baseUrlDev = 'http://10.0.2.2:8080/api/v1'; // 模拟器 localhost

  static const Duration connectTimeout = Duration(seconds: 10);
  static const Duration receiveTimeout = Duration(seconds: 30);

  // 认证
  static const String login = '/auth/login';
  static const String register = '/auth/register';
  static const String refresh = '/auth/refresh';
  static const String oauthLogin = '/auth/oauth/login';

  // 机器人
  static const String robots = '/robots';
  static const String compare = '/compare';
  static const String categories = '/categories';
  static const String manufacturers = '/manufacturers';
  static const String domains = '/domains';
  static const String searchSuggest = '/search/suggest';

  // 用户
  static const String usersMe = '/users/me';
  static const String favorites = '/users/me/favorites';
}
