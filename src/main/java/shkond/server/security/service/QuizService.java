package shkond.server.security.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import shkond.server.model.articles.Article;
import shkond.server.model.articles.ArticleCategory;
import shkond.server.model.articles.ArticleImage;
import shkond.server.model.arts.ArtGenre;
import shkond.server.model.quizzes.Answer;
import shkond.server.model.quizzes.Question;
import shkond.server.model.quizzes.QuestionImage;
import shkond.server.model.quizzes.Quiz;
import shkond.server.repository.article.ArticleCategoryRepository;
import shkond.server.repository.article.ArticleImageRepository;
import shkond.server.repository.article.ArticleRepository;
import shkond.server.repository.arts.ArtGenreRepository;
import shkond.server.repository.quizzes.AnswerRepository;
import shkond.server.repository.quizzes.QuestionImageRepository;
import shkond.server.repository.quizzes.QuestionRepository;
import shkond.server.repository.quizzes.QuizRepository;
import shkond.server.request.AddArticleRequest;
import shkond.server.request.AddQuestionRequest;
import shkond.server.request.AnswerRequest;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private ArtGenreRepository artGenreRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private ArticleCategoryRepository articleCategoryRepository;
    @Autowired
    private ArticleImageRepository articleImageRepository;
    @Autowired
    private ImageService imageService;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private AnswerRepository answerRepository;
    @Autowired
    private QuestionImageRepository questionImageRepository;

    @Value("${image.articles-dir}")
    private String assetDir;

    @Value("${image.questions-dir}")
    private String questionDir;

    public Article addArticle(MultipartFile[] files, AddArticleRequest request) {
        Optional<ArtGenre> artGenreOptional = artGenreRepository.findById(request.getGenreId());
        Optional<ArticleCategory> articleCategoryOptional = articleCategoryRepository.findById(request.getArticleCategoryId());



        Article addArticle = new Article(
                request.getTitle(),
                request.getText(),
                articleCategoryOptional.get(),
                artGenreOptional.get()
        );

        Article article = articleRepository.save(addArticle);
        int count = 0;

        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                String imageName = imageService.saveImage(file, assetDir);

                ArticleImage image = new ArticleImage(imageName, article, request.getDescription().get(count));
                count++;
                articleImageRepository.save(image);
            }
        }

        return article;
    }

    public Quiz createQuiz(Article article) {

        Quiz quiz = new Quiz(
                article.getArtGenre(),
                article
        );

        return quizRepository.save(quiz);
    }

    public Boolean addQuestions(MultipartFile[] files, AddQuestionRequest[] questionRequestsList, Quiz quiz) {
        for (AddQuestionRequest request : questionRequestsList) {
            Question question = new Question(
                    request.getQuestion(),
                    quiz,
                    request.getQuestionValue()
            );

            Question savedQuestion = questionRepository.save(question);

            for (AnswerRequest answerReq : request.getAnswerRequestList()) {

                Answer answer = new Answer(
                        savedQuestion,
                        answerReq.getAnswer(),
                        answerReq.isCorrect()
                );

                Answer savedAnswer = answerRepository.save(answer);
            }

            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    String imageName = imageService.saveImage(file, questionDir);

                    QuestionImage image = new QuestionImage(savedQuestion, imageName);
                    questionImageRepository.save(image);
                }
            }
        }

        return true;
    }

}