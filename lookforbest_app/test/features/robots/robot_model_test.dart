import 'package:flutter_test/flutter_test.dart';
import 'package:lookforbest_app/features/robots/domain/robot.dart';

void main() {
  group('Robot.fromJson', () {
    final Map<String, dynamic> fullJson = {
      'id': 1,
      'name': 'IRB 120',
      'nameEn': 'IRB 120',
      'modelNumber': 'IRB120-3/0.6',
      'slug': 'abb-irb-120',
      'description': '小型工业机器人',
      'coverImageUrl': 'https://cdn.test.com/abb-irb-120.jpg',
      'model3dUrl': 'https://cdn.test.com/abb-irb-120.glb',
      'has3dModel': true,
      'hasVideo': false,
      'manufacturer': {
        'id': 10,
        'name': 'ABB',
        'logoUrl': 'https://cdn.test.com/abb-logo.png',
        'country': 'Switzerland',
      },
      'category': {
        'id': 2,
        'name': '工业机器人',
        'slug': 'industrial',
      },
      'specs': {
        'payloadKg': 3.0,
        'reachMm': 580,
        'dof': 6,
        'repeatabilityMm': 0.01,
        'maxSpeedDegS': 250.0,
        'weightKg': 25.0,
        'ipRating': 'IP54',
        'mounting': 'floor',
      },
      'priceRange': 'mid',
      'status': 'active',
      'viewCount': 1500,
      'favoriteCount': 200,
      'isFavorited': false,
      'images': [
        {'id': 1, 'url': 'https://cdn.test.com/img1.jpg', 'thumbnailUrl': null, 'title': '正面'},
      ],
      'videos': [],
    };

    test('解析完整 JSON 字段', () {
      final robot = Robot.fromJson(fullJson);

      expect(robot.id, equals(1));
      expect(robot.name, equals('IRB 120'));
      expect(robot.nameEn, equals('IRB 120'));
      expect(robot.modelNumber, equals('IRB120-3/0.6'));
      expect(robot.slug, equals('abb-irb-120'));
      expect(robot.description, equals('小型工业机器人'));
      expect(robot.coverImageUrl, equals('https://cdn.test.com/abb-irb-120.jpg'));
      expect(robot.has3dModel, isTrue);
      expect(robot.hasVideo, isFalse);
      expect(robot.status, equals('active'));
      expect(robot.viewCount, equals(1500));
      expect(robot.favoriteCount, equals(200));
      expect(robot.isFavorited, isFalse);
    });

    test('解析嵌套 manufacturer 对象', () {
      final robot = Robot.fromJson(fullJson);

      expect(robot.manufacturer.id, equals(10));
      expect(robot.manufacturer.name, equals('ABB'));
      expect(robot.manufacturer.country, equals('Switzerland'));
    });

    test('解析嵌套 category 对象', () {
      final robot = Robot.fromJson(fullJson);

      expect(robot.category.id, equals(2));
      expect(robot.category.name, equals('工业机器人'));
      expect(robot.category.slug, equals('industrial'));
    });

    test('解析 specs 技术参数', () {
      final robot = Robot.fromJson(fullJson);

      expect(robot.specs.payloadKg, equals(3.0));
      expect(robot.specs.reachMm, equals(580));
      expect(robot.specs.dof, equals(6));
      expect(robot.specs.ipRating, equals('IP54'));
    });

    test('解析 images 媒体列表', () {
      final robot = Robot.fromJson(fullJson);

      expect(robot.images, hasLength(1));
      expect(robot.images.first.url, equals('https://cdn.test.com/img1.jpg'));
      expect(robot.images.first.title, equals('正面'));
    });

    test('空 videos 列表解析为空数组', () {
      final robot = Robot.fromJson(fullJson);
      expect(robot.videos, isEmpty);
    });

    test('缺少可选字段时使用默认值', () {
      final minimalJson = <String, dynamic>{
        'id': 2,
        'name': 'Test Robot',
        'modelNumber': 'TR-001',
        'slug': 'test-robot',
        'manufacturer': <String, dynamic>{'id': 1, 'name': 'TestMaker'},
        'category': <String, dynamic>{'id': 1, 'name': '测试'},
        'specs': <String, dynamic>{},
      };

      final robot = Robot.fromJson(minimalJson);

      expect(robot.has3dModel, isFalse);
      expect(robot.hasVideo, isFalse);
      expect(robot.viewCount, equals(0));
      expect(robot.favoriteCount, equals(0));
      expect(robot.isFavorited, isFalse);
      expect(robot.images, isEmpty);
      expect(robot.videos, isEmpty);
      expect(robot.status, equals('active'));
    });
  });

  group('RobotSummary.fromJson', () {
    test('解析机器人列表卡片数据', () {
      final json = {
        'id': 5,
        'name': 'UR5e',
        'slug': 'universal-robots-ur5e',
        'coverImageUrl': 'https://cdn.test.com/ur5e.jpg',
        'manufacturer': {'id': 2, 'name': 'Universal Robots'},
        'category': {'id': 1, 'name': '协作机器人'},
        'payloadKg': 5.0,
        'reachMm': 850,
        'dof': 6,
        'has3dModel': true,
        'priceRange': 'mid',
        'viewCount': 3000,
        'favoriteCount': 500,
      };

      final summary = RobotSummary.fromJson(json);

      expect(summary.id, equals(5));
      expect(summary.name, equals('UR5e'));
      expect(summary.payloadKg, equals(5.0));
      expect(summary.reachMm, equals(850));
      expect(summary.has3dModel, isTrue);
      expect(summary.manufacturer.name, equals('Universal Robots'));
    });

    test('可选字段为 null 时正常解析', () {
      final json = {
        'id': 6,
        'name': 'Generic Robot',
        'slug': 'generic-robot',
        'manufacturer': {'id': 1, 'name': 'Generic'},
        'category': {'id': 1, 'name': '未分类'},
        'has3dModel': false,
        'viewCount': 0,
        'favoriteCount': 0,
      };

      final summary = RobotSummary.fromJson(json);

      expect(summary.payloadKg, isNull);
      expect(summary.reachMm, isNull);
      expect(summary.dof, isNull);
      expect(summary.coverImageUrl, isNull);
      expect(summary.priceRange, isNull);
    });
  });

  group('RobotFilter', () {
    test('默认值正确', () {
      const filter = RobotFilter();

      expect(filter.sort, equals('relevance'));
      expect(filter.page, equals(0));
      expect(filter.size, equals(20));
    });

    test('toQueryParams 仅包含非空字段', () {
      const filter = RobotFilter(
        q: 'ABB',
        categoryId: 1,
        page: 0,
        size: 20,
      );

      final params = filter.toQueryParams();

      expect(params['q'], equals('ABB'));
      expect(params['categoryId'], equals(1));
      expect(params.containsKey('manufacturerId'), isFalse);
      expect(params.containsKey('payloadMin'), isFalse);
    });

    test('toQueryParams 空字符串 q 不包含在参数中', () {
      const filter = RobotFilter(q: '');
      final params = filter.toQueryParams();

      expect(params.containsKey('q'), isFalse);
    });

    test('copyWith 保留未修改字段', () {
      const original = RobotFilter(q: 'ABB', categoryId: 1, page: 2);
      final updated = original.copyWith(q: 'KUKA');

      expect(updated.q, equals('KUKA'));
      expect(updated.categoryId, equals(1));
      expect(updated.page, equals(2));
    });

    test('domainIds 使用逗号连接', () {
      const filter = RobotFilter(domainIds: [1, 2, 3]);
      final params = filter.toQueryParams();

      expect(params['domainIds'], equals('1,2,3'));
    });
  });

  group('RobotSpecs.fromJson', () {
    test('所有字段为 null 时正常解析', () {
      final specs = RobotSpecs.fromJson({});

      expect(specs.payloadKg, isNull);
      expect(specs.reachMm, isNull);
      expect(specs.dof, isNull);
    });

    test('int 转 double 解析', () {
      final specs = RobotSpecs.fromJson({'payloadKg': 5, 'weightKg': 20});

      expect(specs.payloadKg, equals(5.0));
      expect(specs.weightKg, equals(20.0));
    });
  });
}
