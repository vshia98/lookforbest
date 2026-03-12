package com.lookforbest.controller;

import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.entity.RobotModel;
import com.lookforbest.repository.RobotModelRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/robots")
@RequiredArgsConstructor
@Tag(name = "机器人3D模型", description = "机器人3D模型信息接口")
public class RobotModelController {

    private final RobotModelRepository robotModelRepository;

    @GetMapping("/{id}/model")
    @Operation(summary = "获取机器人3D模型信息")
    public ApiResponse<?> getModel(@PathVariable Long id) {
        return robotModelRepository.findByRobotId(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.ok(null));
    }

    @PutMapping("/{id}/model")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @Operation(summary = "更新机器人3D模型信息（管理员）")
    public ApiResponse<?> updateModel(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        RobotModel model = robotModelRepository.findByRobotId(id)
                .orElseGet(() -> {
                    RobotModel m = new RobotModel();
                    m.setRobotId(id);
                    return m;
                });

        if (body.containsKey("modelUrl")) model.setModelUrl(body.get("modelUrl"));
        if (body.containsKey("posterUrl")) model.setPosterUrl(body.get("posterUrl"));
        if (body.containsKey("arUrl")) model.setArUrl(body.get("arUrl"));
        if (body.containsKey("title")) model.setTitle(body.get("title"));
        if (body.containsKey("annotations")) model.setAnnotations(body.get("annotations"));

        return ApiResponse.ok(robotModelRepository.save(model));
    }
}
