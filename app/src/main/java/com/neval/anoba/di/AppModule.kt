package com.neval.anoba.di

import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.room.Room
import coil3.ImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.request.crossfade
import coil3.video.VideoFrameDecoder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.neval.anoba.chat.call.CallViewModel
import com.neval.anoba.chat.general.GeneralChatRepository
import com.neval.anoba.chat.general.GeneralChatViewModel
import com.neval.anoba.chat.group.GroupChatRepository
import com.neval.anoba.chat.group.GroupChatViewModel
import com.neval.anoba.chat.privatechat.PrivateChatRepository
import com.neval.anoba.chat.privatechat.PrivateChatViewModel
import com.neval.anoba.common.database.AppDatabase
import com.neval.anoba.follow.FollowRepository
import com.neval.anoba.follow.FollowViewModel
import com.neval.anoba.common.repository.IUserRepository
import com.neval.anoba.common.repository.UserRepository
import com.neval.anoba.common.services.AuthService
import com.neval.anoba.common.services.AuthServiceInterface
import com.neval.anoba.common.utils.AnalyticsHelper
import com.neval.anoba.common.utils.EmailValidator
import com.neval.anoba.common.utils.EmailValidatorImpl
import com.neval.anoba.common.utils.ResourceProvider
import com.neval.anoba.common.utils.ResourceProviderImpl
import com.neval.anoba.common.viewmodel.AuthViewModel
import com.neval.anoba.home.HomeViewModel
import com.neval.anoba.letter.LetterEditViewModel
import com.neval.anoba.letter.LetterRepository
import com.neval.anoba.letter.LetterViewModel
import com.neval.anoba.letter.lettercomment.LetterCommentRepository
import com.neval.anoba.letter.lettercomment.LetterCommentViewModel
import com.neval.anoba.letter.letteremoji.LetterEmojiReactionRepository
import com.neval.anoba.market.MarketDao
import com.neval.anoba.market.MarketRepository
import com.neval.anoba.market.MarketViewModel
import com.neval.anoba.login.repository.ILoginRepository
import com.neval.anoba.login.repository.LoginRepository
import com.neval.anoba.login.viewmodel.ForgotPasswordViewModel
import com.neval.anoba.login.viewmodel.LoginViewModel
import com.neval.anoba.login.viewmodel.NewPasswordViewModel
import com.neval.anoba.login.viewmodel.SignUpViewModel
import com.neval.anoba.mete.MeteRepository
import com.neval.anoba.mete.MeteSesViewModel
import com.neval.anoba.mycontent.MyContentViewModel
import com.neval.anoba.photo.PhotoEditViewModel
import com.neval.anoba.photo.PhotoRepository
import com.neval.anoba.photo.PhotoViewModel
import com.neval.anoba.photo.photocomment.PhotoCommentRepository
import com.neval.anoba.photo.photocomment.PhotoCommentViewModel
import com.neval.anoba.photo.photoemoji.PhotoEmojiReactionRepository
import com.neval.anoba.ses.SesEditViewModel
import com.neval.anoba.ses.SesRepository
import com.neval.anoba.ses.SesViewModel
import com.neval.anoba.ses.sescomment.SesCommentRepository
import com.neval.anoba.ses.sescomment.SesCommentViewModel
import com.neval.anoba.ses.sesemoji.SesEmojiReactionRepository
import com.neval.anoba.video.CameraViewModel
import com.neval.anoba.video.VideoEditViewModel
import com.neval.anoba.video.VideoRepository
import com.neval.anoba.video.VideoViewModel
import com.neval.anoba.video.videocomment.VideoCommentRepository
import com.neval.anoba.video.videocomment.VideoCommentViewModel
import com.neval.anoba.video.videoemoji.VideoEmojiReactionRepository
import com.neval.anoba.guide.GuideViewModel
import com.neval.anoba.mosaic.MosaicViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

@androidx.annotation.OptIn(UnstableApi::class)
fun createAppModuleDefinition(
    authInstance: FirebaseAuth,
    firestoreInstance: FirebaseFirestore,
    storageInstance: FirebaseStorage
) = module {

    // --- TEMEL BAĞIMLILIKLAR ---
    single { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    single { authInstance }
    single { firestoreInstance }
    single { storageInstance }
    single<EmailValidator> { EmailValidatorImpl() }
    single<ResourceProvider> { ResourceProviderImpl(androidContext()) }
    single { AnalyticsHelper(androidContext()) }

    // --- DATABASE (ROOM) ---
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "anoba_database"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }
    single { get<AppDatabase>().sesDao() }
    single { get<AppDatabase>().photoDao() }
    single { get<AppDatabase>().videoDao() }
    single { get<AppDatabase>().letterDao() }
    single { get<AppDatabase>().chatDao() }
    single { get<AppDatabase>().pinnedItemDao() }
    single { get<AppDatabase>().followedUserDao() }
    single { get<AppDatabase>().marketDao() }

    // --- REPOSITORY & SERVICE BAĞIMLILIKLARI ---
    single<FollowRepository> { com.neval.anoba.follow.FollowRepositoryImpl(get(), get(), get()) }
    single { LetterRepository(get(), get(), get()) }
    single { SesRepository(get(), get(), get()) }
    single { PhotoRepository(get(), get(), get()) }
    single { VideoRepository(get(), get(), get()) }
    single { MarketRepository(get(), get(), get(), get()) }
    single { com.neval.anoba.common.repository.PinnedItemRepository(get()) }

    single<AuthServiceInterface> { AuthService(get(), get(), get(), get(), androidContext(), get(), get(), get(), get(), get()) }
    single<IUserRepository> { UserRepository(androidContext(), get(), get()) }
    single<ILoginRepository> { LoginRepository(get()) }
    single { GeneralChatRepository(get(), get(), get()) }
    single { GroupChatRepository(get(), get(), get()) }
    single { PrivateChatRepository(get(), get(), get()) }

    single { LetterCommentRepository(get()) }
    single { LetterEmojiReactionRepository(firestore = get(), letterDao = get()) }

    // 🚀 DÜZELTME: SesEmojiReactionRepository artık sesDao alıyor
    single { SesEmojiReactionRepository(firestore = get(), sesRepository = get(), sesDao = get()) }
    single { SesCommentRepository(firestore = get(), sesRepository = get()) }

    single { PhotoEmojiReactionRepository(firestore = get(), photoDao = get()) }
    single { PhotoCommentRepository(get()) }

    single { VideoEmojiReactionRepository(firestore = get(), videoDao = get()) }
    single { VideoCommentRepository(firestore = get(), videoRepository = get()) }

    single { MeteRepository(get()) }
    single { com.neval.anoba.chat.call.CallRepository(get()) }

    single<ImageLoader> {
        ImageLoader.Builder(androidContext())
            .components {
                add(VideoFrameDecoder.Factory())
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .crossfade(true)
            .build()
    }

    // --- VIEWMODEL BAĞIMLILIKLAR ---
    viewModel { AuthViewModel(get(), get(), get()) }
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignUpViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::NewPasswordViewModel)

    viewModelOf(::GeneralChatViewModel)
    viewModelOf(::GroupChatViewModel)
    viewModelOf(::PrivateChatViewModel)
    viewModelOf(::CallViewModel)

    viewModelOf(::FollowViewModel)
    viewModelOf(::MarketViewModel)
    viewModel {
        HomeViewModel(
            letterRepository = get(),
            photoRepository = get(),
            videoRepository = get(),
            sesRepository = get(),
            marketRepository = get(),
            firestore = get(),
            authService = get(),
            pinnedItemRepository = get(),
            followRepository = get(),
            photoReactionRepository = get(),
            videoReactionRepository = get()
        )
    }
    viewModelOf(::LetterViewModel)

    viewModel { (letterId: String) -> LetterCommentViewModel(letterId, get(), get()) }
    viewModel { (letterId: String) -> LetterEditViewModel(letterId, get()) }

    viewModel {
        SesViewModel(
            sesRepository = get(),
            authService = get(),
            reactionRepository = get(),
            analyticsHelper = get(),
            resourceProvider = get(),
            pinnedItemRepository = get(),
            userRepository = get()
        )
    }
    viewModel { (sesId: String) -> SesEditViewModel(sesId, get()) }

    viewModel { (sesId: String) -> SesCommentViewModel(sesId, get(), get()) }

    viewModelOf(::PhotoViewModel)
    viewModel { (photoId: String) -> PhotoCommentViewModel(photoId, get(), get()) }
    viewModel { (photoId: String) -> PhotoEditViewModel(photoId, get()) }

    viewModel {
        VideoViewModel(
            videoRepository = get(),
            reactionRepository = get(),
            authService = get(),
            analyticsHelper = get(),
            resourceProvider = get(),
            pinnedItemRepository = get()
        )
    }
    viewModel { (videoId: String) -> VideoCommentViewModel(videoId, get(), get()) }
    viewModelOf(::VideoEditViewModel)

    viewModelOf(::CameraViewModel)
    viewModelOf(::MyContentViewModel)
    viewModelOf(::MeteSesViewModel)
    viewModelOf(::GuideViewModel)
    viewModelOf(::MosaicViewModel)

}.apply {
    Log.d("AppModule", "Dependency injection module loaded successfully!")
}