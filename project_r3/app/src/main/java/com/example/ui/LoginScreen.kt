package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var activationCode by remember { mutableStateOf("") }
    val isLoggingIn by viewModel.isLoggingIn.collectAsState()
    val loginError by viewModel.loginError.collectAsState()
    val themeAccent by viewModel.themeAccent.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    // Premium logo matches gradient (Neon Purple to Vivid Orange)
    val cyberGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF8A2BE2),
            Color(0xFFD500F9),
            Color(0xFFFF4500),
            Color(0xFFFF8C00)
        )
    )

    val cinematicBackground = Brush.radialGradient(
        colors = listOf(
            Color(0xFF140C24), // Center glowing purple cinema backlight
            Color(0xFF000000)  // Pure black OLED boundaries
        ),
        radius = 1600f
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cinematicBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Top right language switcher row with flags
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Triple("ar", "العربية", "🇸🇦"),
                Triple("en", "English", "🇺🇸"),
                Triple("fr", "Français", "🇫🇷")
            ).forEach { (code, name, flag) ->
                val isSelected = appLanguage == code
                Button(
                    onClick = { viewModel.setAppLanguage(code) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF131519)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(36.dp)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Text(
                        text = "$flag $name",
                        color = if (isSelected) Color.Black else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 60.dp) // Leave room for top bar
        ) {
            // 1. App Logo / Graphic
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.loop_live_logo),
                contentDescription = "Loop Live Play",
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(
                        width = 1.5.dp,
                        brush = cyberGradient,
                        shape = RoundedCornerShape(32.dp)
                    )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Headings
            Text(
                text = "LOOP LIVE",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (appLanguage == "ar") "بوابة البث والقنوات المباشرة الذكية" else if (appLanguage == "fr") "Portail Intelligent de Streaming Direct" else "Smart Live TV & Streaming Portal",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Login Box (Glassmorphic look)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = LocaleHelper.translate("activation_code", appLanguage),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        text = LocaleHelper.translate("enter_code", appLanguage),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    // Error text area with animation
                    AnimatedVisibility(
                        visible = loginError != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = loginError ?: "",
                            color = Color(0xFFFF5252),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }

                    // Input field styled beautifully with Cyan outline
                    OutlinedTextField(
                        value = activationCode,
                        onValueChange = { activationCode = it },
                        placeholder = {
                            Text(
                                text = LocaleHelper.translate("code_example", appLanguage),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = Color.Gray.copy(alpha = 0.5f)
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Code key icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Login Action Button with glowing Cyberpunk effect
                    Button(
                        onClick = { viewModel.login(activationCode) },
                        enabled = !isLoggingIn,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (isLoggingIn) Brush.linearGradient(
                                        listOf(
                                            Color.DarkGray,
                                            Color.Gray
                                        )
                                    ) else cyberGradient
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoggingIn) {
                                CircularProgressIndicator(
                                    color = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = LocaleHelper.translate("login", appLanguage),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 4. Subtle security mention at bottom
            Text(
                text = LocaleHelper.translate("rights_reserved", appLanguage),
                color = Color.White.copy(alpha = 0.2f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
