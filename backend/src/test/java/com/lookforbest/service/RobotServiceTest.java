package com.lookforbest.service;

import com.lookforbest.entity.Manufacturer;
import com.lookforbest.entity.Robot;
import com.lookforbest.entity.RobotCategory;
import com.lookforbest.exception.ResourceNotFoundException;
import com.lookforbest.repository.ApplicationDomainRepository;
import com.lookforbest.repository.ManufacturerRepository;
import com.lookforbest.repository.RobotCategoryRepository;
import com.lookforbest.repository.RobotRepository;
import com.lookforbest.service.impl.RobotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RobotService 单元测试")
class RobotServiceTest {

    @Mock
    private RobotRepository robotRepository;
    @Mock
    private ManufacturerRepository manufacturerRepository;
    @Mock
    private RobotCategoryRepository categoryRepository;
    @Mock
    private ApplicationDomainRepository domainRepository;

    @InjectMocks
    private RobotServiceImpl robotService;

    private Robot testRobot;
    private Manufacturer testManufacturer;
    private RobotCategory testCategory;

    @BeforeEach
    void setUp() {
        testManufacturer = new Manufacturer();
        testManufacturer.setId(1L);
        testManufacturer.setName("ABB");

        testCategory = new RobotCategory();
        testCategory.setId(1L);
        testCategory.setName("工业机器人");

        testRobot = new Robot();
        testRobot.setId(1L);
        testRobot.setName("IRB 120");
        testRobot.setSlug("abb-irb-120");
        testRobot.setManufacturer(testManufacturer);
        testRobot.setCategory(testCategory);
        testRobot.setViewCount(100L);
    }

    @Test
    @DisplayName("findById - 查询成功并增加浏览量")
    void findById_success_incrementsViewCount() {
        when(robotRepository.findById(1L)).thenReturn(Optional.of(testRobot));
        when(robotRepository.save(any(Robot.class))).thenReturn(testRobot);

        Robot result = robotService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("IRB 120");
        assertThat(testRobot.getViewCount()).isEqualTo(101L);
        verify(robotRepository).save(testRobot);
    }

    @Test
    @DisplayName("findById - 不存在时抛出 ResourceNotFoundException")
    void findById_notFound_throwsException() {
        when(robotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> robotService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Robot");
    }

    @Test
    @DisplayName("findBySlug - 查询成功")
    void findBySlug_success() {
        when(robotRepository.findBySlug("abb-irb-120")).thenReturn(Optional.of(testRobot));

        Robot result = robotService.findBySlug("abb-irb-120");

        assertThat(result.getSlug()).isEqualTo("abb-irb-120");
    }

    @Test
    @DisplayName("findBySlug - slug不存在时抛出异常")
    void findBySlug_notFound_throwsException() {
        when(robotRepository.findBySlug("not-exist")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> robotService.findBySlug("not-exist"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("create - 自动生成 slug 并保存")
    void create_autoGeneratesSlug() {
        Robot newRobot = new Robot();
        newRobot.setName("IRB 1200");
        newRobot.setManufacturer(testManufacturer);

        when(robotRepository.findBySlug(anyString())).thenReturn(Optional.empty());
        when(robotRepository.save(any(Robot.class))).thenAnswer(inv -> inv.getArgument(0));

        Robot result = robotService.create(newRobot);

        assertThat(result.getSlug()).isNotBlank();
        assertThat(result.getSlug()).contains("abb");
        verify(robotRepository).save(newRobot);
    }

    @Test
    @DisplayName("create - 指定 slug 时不自动生成")
    void create_withExistingSlug_doesNotOverride() {
        Robot newRobot = new Robot();
        newRobot.setName("IRB 1200");
        newRobot.setSlug("custom-slug");
        newRobot.setManufacturer(testManufacturer);

        when(robotRepository.save(any(Robot.class))).thenAnswer(inv -> inv.getArgument(0));

        Robot result = robotService.create(newRobot);

        assertThat(result.getSlug()).isEqualTo("custom-slug");
    }

    @Test
    @DisplayName("update - 更新机器人属性")
    void update_success() {
        Robot updated = new Robot();
        updated.setName("IRB 120 Pro");
        updated.setPayloadKg(new BigDecimal("3.0"));

        when(robotRepository.findById(1L)).thenReturn(Optional.of(testRobot));
        when(robotRepository.save(any(Robot.class))).thenAnswer(inv -> inv.getArgument(0));

        Robot result = robotService.update(1L, updated);

        assertThat(result.getName()).isEqualTo("IRB 120 Pro");
        assertThat(result.getPayloadKg()).isEqualByComparingTo(new BigDecimal("3.0"));
    }

    @Test
    @DisplayName("update - 目标不存在时抛出异常")
    void update_notFound_throwsException() {
        when(robotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> robotService.update(99L, new Robot()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("delete - 删除成功")
    void delete_success() {
        when(robotRepository.existsById(1L)).thenReturn(true);

        assertThatCode(() -> robotService.delete(1L)).doesNotThrowAnyException();
        verify(robotRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete - 不存在时抛出异常")
    void delete_notFound_throwsException() {
        when(robotRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> robotService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(robotRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("findAll - 带过滤条件分页查询")
    void findAll_withFilters() {
        Page<Robot> mockPage = new PageImpl<>(List.of(testRobot));
        when(robotRepository.findWithFilters(any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockPage);

        Page<Robot> result = robotService.findAll(
                "ABB", 1L, 1L, null,
                null, null, null, null,
                null, null, null, "active", "relevance",
                PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("IRB 120");
    }

    @Test
    @DisplayName("findSimilar - 查询相似机器人")
    void findSimilar_success() {
        when(robotRepository.findById(1L)).thenReturn(Optional.of(testRobot));
        when(robotRepository.findTop6ByCategoryAndIdNotOrderByViewCountDesc(testCategory, 1L))
                .thenReturn(List.of(testRobot));

        List<Robot> result = robotService.findSimilar(1L, 6);

        assertThat(result).hasSize(1);
    }
}
