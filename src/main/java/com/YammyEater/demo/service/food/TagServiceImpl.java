package com.YammyEater.demo.service.food;

import com.YammyEater.demo.domain.food.Tag;
import com.YammyEater.demo.dto.food.TagDto;
import com.YammyEater.demo.repository.food.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    public List<TagDto> getAllTag() {
        List<Tag> tags = tagRepository.findAll();
        return tags.stream().map(TagDto::of).toList();
    }
}
