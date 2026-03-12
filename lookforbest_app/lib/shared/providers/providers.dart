import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../../core/network/dio_client.dart';
import '../../core/storage/secure_storage.dart';
import '../../features/auth/data/auth_service.dart';
import '../../features/robots/data/robot_service.dart';

export 'compare_provider.dart';

// 基础设施 Providers
final secureStorageProvider = Provider<FlutterSecureStorage>(
  (_) => const FlutterSecureStorage(),
);

final secureStorageServiceProvider = Provider<SecureStorage>(
  (ref) => SecureStorage(ref.read(secureStorageProvider)),
);

final dioClientProvider = Provider<DioClient>(
  (ref) => DioClient(storage: ref.read(secureStorageProvider)),
);

// 服务层 Providers
final authServiceProvider = Provider<AuthService>(
  (ref) => AuthService(
    ref.read(dioClientProvider).dio,
    ref.read(secureStorageServiceProvider),
  ),
);

final robotServiceProvider = Provider<RobotService>(
  (ref) => RobotService(ref.read(dioClientProvider).dio),
);
