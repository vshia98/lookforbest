/// 机器人数据模型（对应 API /robots/{id} 响应）
class Robot {
  final int id;
  final String name;
  final String? nameEn;
  final String modelNumber;
  final String slug;
  final String? description;
  final String? coverImageUrl;
  final String? model3dUrl;
  final bool has3dModel;
  final bool hasVideo;
  final RobotManufacturer manufacturer;
  final RobotCategory category;
  final RobotSpecs specs;
  final String? priceRange;
  final String status;
  final int viewCount;
  final int favoriteCount;
  final bool isFavorited;
  final List<RobotMedia> images;
  final List<RobotMedia> videos;

  const Robot({
    required this.id,
    required this.name,
    this.nameEn,
    required this.modelNumber,
    required this.slug,
    this.description,
    this.coverImageUrl,
    this.model3dUrl,
    required this.has3dModel,
    required this.hasVideo,
    required this.manufacturer,
    required this.category,
    required this.specs,
    this.priceRange,
    required this.status,
    required this.viewCount,
    required this.favoriteCount,
    required this.isFavorited,
    required this.images,
    required this.videos,
  });

  factory Robot.fromJson(Map<String, dynamic> json) {
    return Robot(
      id: json['id'] as int,
      name: json['name'] as String,
      nameEn: json['nameEn'] as String?,
      modelNumber: json['modelNumber'] as String,
      slug: json['slug'] as String,
      description: json['description'] as String?,
      coverImageUrl: json['coverImageUrl'] as String?,
      model3dUrl: json['model3dUrl'] as String?,
      has3dModel: json['has3dModel'] as bool? ?? false,
      hasVideo: json['hasVideo'] as bool? ?? false,
      manufacturer: RobotManufacturer.fromJson(
        json['manufacturer'] as Map<String, dynamic>,
      ),
      category: RobotCategory.fromJson(
        json['category'] as Map<String, dynamic>,
      ),
      specs: RobotSpecs.fromJson(json['specs'] as Map<String, dynamic>),
      priceRange: json['priceRange'] as String?,
      status: json['status'] as String? ?? 'active',
      viewCount: json['viewCount'] as int? ?? 0,
      favoriteCount: json['favoriteCount'] as int? ?? 0,
      isFavorited: json['isFavorited'] as bool? ?? false,
      images: (json['images'] as List<dynamic>?)
              ?.map((e) => RobotMedia.fromJson(e as Map<String, dynamic>))
              .toList() ??
          [],
      videos: (json['videos'] as List<dynamic>?)
              ?.map((e) => RobotMedia.fromJson(e as Map<String, dynamic>))
              .toList() ??
          [],
    );
  }
}

/// 机器人列表卡片（精简版）
class RobotSummary {
  final int id;
  final String name;
  final String slug;
  final String? coverImageUrl;
  final RobotManufacturer manufacturer;
  final RobotCategory category;
  final double? payloadKg;
  final int? reachMm;
  final int? dof;
  final bool has3dModel;
  final String? priceRange;
  final int viewCount;
  final int favoriteCount;

  const RobotSummary({
    required this.id,
    required this.name,
    required this.slug,
    this.coverImageUrl,
    required this.manufacturer,
    required this.category,
    this.payloadKg,
    this.reachMm,
    this.dof,
    required this.has3dModel,
    this.priceRange,
    required this.viewCount,
    required this.favoriteCount,
  });

  factory RobotSummary.fromJson(Map<String, dynamic> json) {
    return RobotSummary(
      id: json['id'] as int,
      name: json['name'] as String,
      slug: json['slug'] as String,
      coverImageUrl: json['coverImageUrl'] as String?,
      manufacturer: RobotManufacturer.fromJson(
        json['manufacturer'] as Map<String, dynamic>,
      ),
      category: RobotCategory.fromJson(
        json['category'] as Map<String, dynamic>,
      ),
      payloadKg: (json['payloadKg'] as num?)?.toDouble(),
      reachMm: json['reachMm'] as int?,
      dof: json['dof'] as int?,
      has3dModel: json['has3dModel'] as bool? ?? false,
      priceRange: json['priceRange'] as String?,
      viewCount: json['viewCount'] as int? ?? 0,
      favoriteCount: json['favoriteCount'] as int? ?? 0,
    );
  }
}

class RobotManufacturer {
  final int id;
  final String name;
  final String? logoUrl;
  final String? country;

  const RobotManufacturer({
    required this.id,
    required this.name,
    this.logoUrl,
    this.country,
  });

  factory RobotManufacturer.fromJson(Map<String, dynamic> json) {
    return RobotManufacturer(
      id: json['id'] as int,
      name: json['name'] as String,
      logoUrl: json['logoUrl'] as String?,
      country: json['country'] as String?,
    );
  }
}

class RobotCategory {
  final int id;
  final String name;
  final String? slug;

  const RobotCategory({required this.id, required this.name, this.slug});

  factory RobotCategory.fromJson(Map<String, dynamic> json) {
    return RobotCategory(
      id: json['id'] as int,
      name: json['name'] as String,
      slug: json['slug'] as String?,
    );
  }
}

class RobotSpecs {
  final double? payloadKg;
  final int? reachMm;
  final int? dof;
  final double? repeatabilityMm;
  final double? maxSpeedDegS;
  final double? weightKg;
  final String? ipRating;
  final String? mounting;

  const RobotSpecs({
    this.payloadKg,
    this.reachMm,
    this.dof,
    this.repeatabilityMm,
    this.maxSpeedDegS,
    this.weightKg,
    this.ipRating,
    this.mounting,
  });

  factory RobotSpecs.fromJson(Map<String, dynamic> json) {
    return RobotSpecs(
      payloadKg: (json['payloadKg'] as num?)?.toDouble(),
      reachMm: json['reachMm'] as int?,
      dof: json['dof'] as int?,
      repeatabilityMm: (json['repeatabilityMm'] as num?)?.toDouble(),
      maxSpeedDegS: (json['maxSpeedDegS'] as num?)?.toDouble(),
      weightKg: (json['weightKg'] as num?)?.toDouble(),
      ipRating: json['ipRating'] as String?,
      mounting: json['mounting'] as String?,
    );
  }
}

class RobotMedia {
  final int id;
  final String url;
  final String? thumbnailUrl;
  final String? title;

  const RobotMedia({
    required this.id,
    required this.url,
    this.thumbnailUrl,
    this.title,
  });

  factory RobotMedia.fromJson(Map<String, dynamic> json) {
    return RobotMedia(
      id: json['id'] as int,
      url: json['url'] as String,
      thumbnailUrl: json['thumbnailUrl'] as String?,
      title: json['title'] as String?,
    );
  }
}

/// 机器人筛选参数
class RobotFilter {
  final String? q;
  final int? categoryId;
  final int? manufacturerId;
  final List<int>? domainIds;
  final double? payloadMin;
  final double? payloadMax;
  final int? reachMin;
  final int? reachMax;
  final int? dofMin;
  final int? dofMax;
  final bool? has3dModel;
  final String? priceRange;
  final String sort;
  final int page;
  final int size;

  const RobotFilter({
    this.q,
    this.categoryId,
    this.manufacturerId,
    this.domainIds,
    this.payloadMin,
    this.payloadMax,
    this.reachMin,
    this.reachMax,
    this.dofMin,
    this.dofMax,
    this.has3dModel,
    this.priceRange,
    this.sort = 'relevance',
    this.page = 0,
    this.size = 20,
  });

  Map<String, dynamic> toQueryParams() {
    return {
      if (q != null && q!.isNotEmpty) 'q': q,
      if (categoryId != null) 'categoryId': categoryId,
      if (manufacturerId != null) 'manufacturerId': manufacturerId,
      if (domainIds != null && domainIds!.isNotEmpty)
        'domainIds': domainIds!.join(','),
      if (payloadMin != null) 'payloadMin': payloadMin,
      if (payloadMax != null) 'payloadMax': payloadMax,
      if (reachMin != null) 'reachMin': reachMin,
      if (reachMax != null) 'reachMax': reachMax,
      if (dofMin != null) 'dofMin': dofMin,
      if (dofMax != null) 'dofMax': dofMax,
      if (has3dModel != null) 'has3dModel': has3dModel,
      if (priceRange != null) 'priceRange': priceRange,
      'sort': sort,
      'page': page,
      'size': size,
    };
  }

  RobotFilter copyWith({
    String? q,
    int? categoryId,
    int? manufacturerId,
    List<int>? domainIds,
    double? payloadMin,
    double? payloadMax,
    int? reachMin,
    int? reachMax,
    int? dofMin,
    int? dofMax,
    bool? has3dModel,
    String? priceRange,
    String? sort,
    int? page,
    int? size,
  }) {
    return RobotFilter(
      q: q ?? this.q,
      categoryId: categoryId ?? this.categoryId,
      manufacturerId: manufacturerId ?? this.manufacturerId,
      domainIds: domainIds ?? this.domainIds,
      payloadMin: payloadMin ?? this.payloadMin,
      payloadMax: payloadMax ?? this.payloadMax,
      reachMin: reachMin ?? this.reachMin,
      reachMax: reachMax ?? this.reachMax,
      dofMin: dofMin ?? this.dofMin,
      dofMax: dofMax ?? this.dofMax,
      has3dModel: has3dModel ?? this.has3dModel,
      priceRange: priceRange ?? this.priceRange,
      sort: sort ?? this.sort,
      page: page ?? this.page,
      size: size ?? this.size,
    );
  }
}
