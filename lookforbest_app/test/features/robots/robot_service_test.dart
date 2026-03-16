import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:lookforbest_app/features/robots/data/robot_service.dart';
import 'package:lookforbest_app/features/robots/domain/robot.dart';

import 'robot_service_test.mocks.dart';

@GenerateMocks([Dio])
void main() {
  late MockDio mockDio;
  late RobotService robotService;

  setUp(() {
    mockDio = MockDio();
    robotService = RobotService(mockDio);
  });

  final Map<String, dynamic> robotListApiResponse = {
    'success': true,
    'code': 200,
    'message': 'ok',
    'data': {
      'content': [
        {
          'id': 1,
          'name': 'IRB 120',
          'slug': 'abb-irb-120',
          'manufacturer': {'id': 1, 'name': 'ABB'},
          'category': {'id': 1, 'name': '工业机器人'},
          'has3dModel': true,
          'viewCount': 100,
          'favoriteCount': 20,
        },
      ],
      'page': 0,
      'size': 20,
      'total': 1,
      'totalPages': 1,
      'hasNext': false,
    },
  };

  final Map<String, dynamic> robotDetailApiResponse = {
    'success': true,
    'code': 200,
    'message': 'ok',
    'data': {
      'id': 1,
      'name': 'IRB 120',
      'nameEn': 'IRB 120',
      'modelNumber': 'IRB120-3/0.6',
      'slug': 'abb-irb-120',
      'manufacturer': {'id': 1, 'name': 'ABB'},
      'category': {'id': 1, 'name': '工业机器人'},
      'specs': {'payloadKg': 3.0, 'reachMm': 580, 'dof': 6},
      'has3dModel': true,
      'hasVideo': false,
      'viewCount': 101,
      'favoriteCount': 20,
      'isFavorited': false,
      'images': [],
      'videos': [],
    },
  };

  group('RobotService.getRobots', () {
    test('成功获取机器人列表', () async {
      when(mockDio.get(any, queryParameters: anyNamed('queryParameters')))
          .thenAnswer((_) async => Response(
                data: robotListApiResponse,
                statusCode: 200,
                requestOptions: RequestOptions(path: '/robots'),
              ));

      const filter = RobotFilter();
      final result = await robotService.getRobots(filter);

      expect(result.content, hasLength(1));
      expect(result.content.first.name, equals('IRB 120'));
      expect(result.total, equals(1));
      expect(result.hasNext, isFalse);
    });

    test('传递筛选参数到 queryParameters', () async {
      when(mockDio.get(any, queryParameters: anyNamed('queryParameters')))
          .thenAnswer((_) async => Response(
                data: {
                  'success': true,
                  'code': 200,
                  'message': 'ok',
                  'data': {
                    'content': [],
                    'page': 0,
                    'size': 20,
                    'total': 0,
                    'totalPages': 0,
                    'hasNext': false,
                  },
                },
                statusCode: 200,
                requestOptions: RequestOptions(path: '/robots'),
              ));

      const filter = RobotFilter(q: 'ABB', categoryId: 1, page: 0, size: 20);
      await robotService.getRobots(filter);

      verify(mockDio.get(
        any,
        queryParameters: argThat(
          predicate<Map<String, dynamic>>(
            (p) => p['q'] == 'ABB' && p['categoryId'] == 1,
          ),
          named: 'queryParameters',
        ),
      ));
    });
  });

  group('RobotService.getRobotById', () {
    test('成功获取机器人详情', () async {
      when(mockDio.get(any, queryParameters: anyNamed('queryParameters')))
          .thenAnswer((_) async => Response(
                data: robotDetailApiResponse,
                statusCode: 200,
                requestOptions: RequestOptions(path: '/robots/1'),
              ));

      final robot = await robotService.getRobotById(1);

      expect(robot.id, equals(1));
      expect(robot.name, equals('IRB 120'));
      expect(robot.specs.payloadKg, equals(3.0));
      expect(robot.viewCount, equals(101));
    });

    test('API 异常时抛出 DioException', () async {
      when(mockDio.get(any, queryParameters: anyNamed('queryParameters')))
          .thenThrow(DioException(
            requestOptions: RequestOptions(path: '/robots/99'),
            response: Response(
              statusCode: 404,
              requestOptions: RequestOptions(path: '/robots/99'),
            ),
            type: DioExceptionType.badResponse,
          ));

      expect(
        () => robotService.getRobotById(99),
        throwsA(isA<DioException>()),
      );
    });
  });

  group('RobotService.getSimilarRobots', () {
    test('成功获取相似机器人列表', () async {
      when(mockDio.get(any, queryParameters: anyNamed('queryParameters')))
          .thenAnswer((_) async => Response(
                data: {
                  'success': true,
                  'code': 200,
                  'message': 'ok',
                  'data': [
                    {
                      'id': 2,
                      'name': 'IRB 140',
                      'slug': 'abb-irb-140',
                      'manufacturer': {'id': 1, 'name': 'ABB'},
                      'category': {'id': 1, 'name': '工业机器人'},
                      'has3dModel': false,
                      'viewCount': 50,
                      'favoriteCount': 10,
                    },
                  ],
                },
                statusCode: 200,
                requestOptions: RequestOptions(path: '/robots/1/similar'),
              ));

      final result = await robotService.getSimilarRobots(1, size: 6);

      expect(result, hasLength(1));
      expect(result.first.name, equals('IRB 140'));
    });
  });

  group('RobotService.searchSuggest', () {
    test('空字符串直接返回空列表，不发请求', () async {
      final result = await robotService.searchSuggest('');

      expect(result, isEmpty);
      verifyNever(mockDio.get(any));
    });

    test('有效查询词发起请求并返回建议', () async {
      when(mockDio.get(any, queryParameters: anyNamed('queryParameters')))
          .thenAnswer((_) async => Response(
                data: {
                  'data': {
                    'suggestions': [
                      {'id': 1, 'name': 'IRB 120', 'slug': 'abb-irb-120'},
                    ],
                  },
                },
                statusCode: 200,
                requestOptions: RequestOptions(path: '/search/suggest'),
              ));

      final result = await robotService.searchSuggest('ABB');

      expect(result, hasLength(1));
      expect(result.first['name'], equals('IRB 120'));
    });
  });
}
