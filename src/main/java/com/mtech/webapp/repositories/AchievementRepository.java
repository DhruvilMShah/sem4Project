package com.mtech.webapp.repositories;
import java.util.List;
import com.mtech.webapp.models.Achievement;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AchievementRepository extends MongoRepository<Achievement,String> {
    List<Achievement> findByEmail(String email);
    List<Achievement> findByCategory(String category);


}
