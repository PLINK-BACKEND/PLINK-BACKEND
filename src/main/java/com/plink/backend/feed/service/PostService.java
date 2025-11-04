package com.plink.backend.feed.service;

import com.plink.backend.feed.entity.Image;
import com.plink.backend.feed.entity.Post;
import com.plink.backend.feed.entity.Tag;
import com.plink.backend.feed.repository.ImageRepository;
import com.plink.backend.feed.repository.PostRepository;
import com.plink.backend.feed.repository.TagRepository;
import com.plink.backend.feed.dto.PostUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final ImageRepository imafeRepository;
    private final S3Service s3Service;

    @Transactional
    // 게시글 작성하기
    public Post createpost(User author, String title, String content,
                           String tagName, List<MultipartFile> images) throws IOException {
        // 이미지 개수 검증
        if (images != null && images.size() > 3) {
            throw new IllegalArgumentException("이미지는 최대 3장까지 업로드 가능합니다.");

        // 태그 찾기
        Tag tag = tagRepository.findByName(tagName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 태그입니다."));

        // 게시글 생성
        Post post = new Post(author, title, content,tag);
        postRepository.save(post);

        // 이미지 업로드
        if (images != null) {
            for (MultipartFile file : images) {
                String key = s3Service.upload(file,"posts");
                Image image = new Image(post,key,file.getOriginalFilename());
                imafeRepository.save(image);
            }
        }
        return post;


    }

    // 게시글 수정
    @Transactional
    public Post updatePost(Long postId, PostUpdateRequest req){
            Post post = postRepository.findById(postId)
                    .orElseThrow(()->new IllegalArgumentException("게시글을 찾을 수 없습니다."));

            // 제목 수정 (값이 들어온 경우에만)
            if (req.getTitle() != null && !req.getTitle().isBlank()) {
                post.updateTitle(req.getTitle());
            }

            // 내용 수정
            if (req.getContent() != null && !req.getContent().isBlank()) {
                post.updateContent(req.getContent());
            }

            // 태그 수정
            if (req.getTagName() != null && !req.getTagName().isBlank()) {
                Tag tag = tagRepository.findByName(req.getTagName())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 태그입니다."));
                post.changeTag(tag);
            }

            return post;
        }



    // 게시글 삭제
    @Transactional
    public Post deletePost(Long postId){
            Post post = postRepository.findById(postId)
                    .orElseThrow(()-> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

            // 이미지 삭제
            for (Image image : post.getImages()) {
                s3Service.delete(img.getS3key());
            }

            postRepository.delete(post);
        }


    // 게시글 조회
    public Post getPost(Long postId){
            return postRepository.findById(postId)
                    .orElseThrow(()-> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        }

    // 게시글 모두 조회
    public List<Post> getPostList(){
            return postRepository.findAll();
        }

}
