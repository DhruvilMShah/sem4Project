package com.mtech.webapp.repositories;
import java.util.List;
import com.mtech.webapp.models.Achievement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementRepository extends MongoRepository<Achievement,String> {
    List<Achievement> findByEmail(String email);
    Achievement findByAchievementId(String achievementId);

    List<Achievement> findByEmailAndCategory(String userEmail, String categoryName);

    List<Achievement> findByEmailAndFromDateAndToDate(String userEmail, String fromDate, String toDate);

    int deleteByAchievementId(String achievementId);
}
