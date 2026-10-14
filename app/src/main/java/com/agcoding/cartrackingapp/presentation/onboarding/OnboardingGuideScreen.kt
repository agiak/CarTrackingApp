package com.agcoding.cartrackingapp.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun OnboardingGuideScreen(
    viewModel: OnboardingViewModel,
    onSkip: () -> Unit,
    onComplete: () -> Unit
) {
    val currentSlideIndex by viewModel.currentSlideIndex.collectAsState()
    val slides = viewModel.slides
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

    val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()

    // Sync pager with viewModel
    LaunchedEffect(currentSlideIndex) {
        pagerState.animateScrollToPage(
            page = currentSlideIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.goToSlide(pagerState.currentPage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = if (isTablet) Alignment.Center else Alignment.TopStart
    ) {
        Column(
            modifier = Modifier
                .then(
                    if (isTablet) Modifier.fillMaxWidth(0.7f) // 70% width on tablets
                    else Modifier.fillMaxWidth()
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Skip button at top right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onSkip) {
                Text(
                    text = stringResource(R.string.skip),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Pager content with custom page transformations
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

            // Apply smooth animations based on page offset
            val alpha by animateFloatAsState(
                targetValue = 1f - (pageOffset.absoluteValue * 0.5f).coerceIn(0f, 0.5f),
                animationSpec = tween(150),
                label = "alpha"
            )

            val scale by animateFloatAsState(
                targetValue = 1f - (pageOffset.absoluteValue * 0.15f).coerceIn(0f, 0.15f),
                animationSpec = tween(150),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.alpha = alpha
                        scaleX = scale
                        scaleY = scale
                        // Add subtle vertical translation for depth effect
                        translationY = pageOffset.absoluteValue * 50f
                    }
            ) {
                SlideContent(
                    slide = slides[page],
                    slideIndex = page,
                    isCurrentPage = page == pagerState.currentPage
                )
            }
        }

        // Animated page indicator dots
        Row(
            modifier = Modifier.padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            slides.forEachIndexed { index, _ ->
                val isSelected = index == pagerState.currentPage

                val width by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 8.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "dotWidth"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width, 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            }
                        )
                )
            }
        }

        // Animated navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Previous button (visible after first slide)
            AnimatedVisibility(
                visible = currentSlideIndex > 0,
                enter = fadeIn(tween(200)) + slideInHorizontally(
                    initialOffsetX = { -it / 2 },
                    animationSpec = tween(200)
                ),
                exit = fadeOut(tween(200)) + slideOutHorizontally(
                    targetOffsetX = { -it / 2 },
                    animationSpec = tween(200)
                )
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                page = currentSlideIndex - 1,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.previous))
                }
            }

            // Next / Get Started button with animated text
            Button(
                onClick = {
                    if (currentSlideIndex < slides.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                page = currentSlideIndex + 1,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier.weight(if (currentSlideIndex > 0) 1f else 2f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                AnimatedContent(
                    targetState = currentSlideIndex == slides.size - 1,
                    transitionSpec = {
                        (fadeIn(tween(200)) + scaleIn(initialScale = 0.9f, animationSpec = tween(200)))
                            .togetherWith(fadeOut(tween(150)) + scaleOut(targetScale = 0.9f, animationSpec = tween(150)))
                    },
                    label = "buttonText"
                ) { isLastSlide ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = if (isLastSlide) stringResource(R.string.get_started) else stringResource(R.string.next))
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun SlideContent(
    slide: OnboardingSlide,
    slideIndex: Int,
    isCurrentPage: Boolean
) {
    val accent = when (slide.accent) {
        SlideAccent.PRIMARY -> MaterialTheme.colorScheme.primary
        SlideAccent.TERTIARY -> MaterialTheme.colorScheme.tertiary
        SlideAccent.SECONDARY -> MaterialTheme.colorScheme.secondary
    }

    val illustrationScale by animateFloatAsState(
        targetValue = if (isCurrentPage) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "illustrationScale"
    )

    // Scrollable, because the steps now carry instructions rather than a
    // slogan, and three bullets plus a drawing do not fit a small phone in
    // landscape — on which the old centred column simply clipped.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.scale(illustrationScale)) {
            SlideIllustration(slideIndex = slideIndex, accent = accent)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(slide.titleRes),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(slide.descriptionRes),
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        if (slide.bulletRes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                slide.bulletRes.forEach { bullet ->
                    SlideBullet(text = stringResource(bullet), accent = accent)
                }
            }
        }
    }
}

/**
 * One instruction. Left-aligned on purpose while everything above it is
 * centred: a centred list of different-length lines has no edge for the eye to
 * follow, and these are meant to be read as steps, not admired.
 */
@Composable
private fun SlideBullet(text: String, accent: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(accent)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Guide slide - first", showBackground = true, widthDp = 400, heightDp = 560)
@Composable
private fun PreviewSlideContentFirst() {
    CarTrackingAppTheme(darkTheme = false) {
        SlideContent(
            slide = OnboardingContent.slides.first(),
            slideIndex = 0,
            isCurrentPage = true
        )
    }
}

/** Off-screen pages are drawn smaller and dimmer; this is what a neighbour looks like. */
@Preview(name = "Guide slide - not current", showBackground = true, widthDp = 400, heightDp = 560)
@Composable
private fun PreviewSlideContentNotCurrent() {
    CarTrackingAppTheme(darkTheme = false) {
        SlideContent(
            slide = OnboardingContent.slides.first(),
            slideIndex = 0,
            isCurrentPage = false
        )
    }
}

@Preview(name = "Guide slide - dark", showBackground = true, widthDp = 400, heightDp = 560)
@Composable
private fun PreviewSlideContentDark() {
    CarTrackingAppTheme(darkTheme = true) {
        SlideContent(
            slide = OnboardingContent.slides.last(),
            slideIndex = OnboardingContent.slides.lastIndex,
            isCurrentPage = true
        )
    }
}
