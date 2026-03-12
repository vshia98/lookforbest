package com.lookforbest.service.impl;

import com.lookforbest.entity.Robot;
import com.lookforbest.entity.User;
import com.lookforbest.entity.UserFavorite;
import com.lookforbest.exception.BusinessException;
import com.lookforbest.exception.ResourceNotFoundException;
import com.lookforbest.repository.RobotRepository;
import com.lookforbest.repository.UserFavoriteRepository;
import com.lookforbest.repository.UserRepository;
import com.lookforbest.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final UserFavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final RobotRepository robotRepository;

    @Override
    @Transactional
    public void addFavorite(String userEmail, Long robotId) {
        User user = getUser(userEmail);
        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new ResourceNotFoundException("Robot", robotId));

        if (favoriteRepository.existsByUserAndRobot(user, robot)) {
            throw new BusinessException(409, "已收藏该机器人");
        }

        UserFavorite favorite = new UserFavorite();
        favorite.setUser(user);
        favorite.setRobot(robot);
        favoriteRepository.save(favorite);

        robot.setFavoriteCount(robot.getFavoriteCount() + 1);
        robotRepository.save(robot);
    }

    @Override
    @Transactional
    public void removeFavorite(String userEmail, Long robotId) {
        User user = getUser(userEmail);
        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new ResourceNotFoundException("Robot", robotId));

        favoriteRepository.deleteByUserAndRobot(user, robot);

        if (robot.getFavoriteCount() > 0) {
            robot.setFavoriteCount(robot.getFavoriteCount() - 1);
            robotRepository.save(robot);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Robot> getFavorites(String userEmail, Pageable pageable) {
        User user = getUser(userEmail);
        return favoriteRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(UserFavorite::getRobot);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorited(String userEmail, Long robotId) {
        if (userEmail == null) return false;
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) return false;
        Robot robot = robotRepository.findById(robotId).orElse(null);
        if (robot == null) return false;
        return favoriteRepository.existsByUserAndRobot(user, robot);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User: " + email));
    }
}
