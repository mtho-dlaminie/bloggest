package com.mcplay.bloggest.repo;

import com.mcplay.bloggest.model.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserRepository extends ElasticsearchRepository<User, String> {
    User findByUsername(String username);
}
