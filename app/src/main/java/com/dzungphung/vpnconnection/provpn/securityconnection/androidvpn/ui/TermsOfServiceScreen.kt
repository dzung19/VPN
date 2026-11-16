package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.R
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.theme.AppColors

@Composable
fun TermsOfServiceScreen(
    onAccepted: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 24.dp)
            .systemBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(22.dp),
                color = AppColors.AccentCoralSoft,
                border = BorderStroke(1.dp, AppColors.AccentCoral)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = null,
                        tint = AppColors.AccentCoral,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Quyền riêng tư & Điều khoản sử dụng",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = AppColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = AppColors.Surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, AppColors.SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = AppColors.AccentEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cam kết bảo vệ quyền riêng tư:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    PolicyItem("100% Xử lý AI cục bộ", "Âm thanh vỗ tay/huýt sáo được phân tích trực tiếp trên máy bằng YAMNet AI, tuyệt đối không gửi lên bất kỳ máy chủ nào.")
                    PolicyItem("Không ghi âm hay lưu trữ", "Dữ liệu âm thanh chỉ xử lý tức thời trong RAM và bị hủy ngay lập tức, không bao giờ lưu trữ thành file.")
                    PolicyItem("Đèn Flash & Rung", "Chỉ được kích hoạt khi chuông báo động reo nhằm giúp bạn tìm thấy máy nhanh nhất.")
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextButton(onClick = { showPrivacyDialog = true }) {
                Text(
                    text = "Xem chi tiết Chính sách bảo mật",
                    color = AppColors.AccentBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onAccepted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.AccentCoral,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Đồng ý & Bắt đầu",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(
            context = context,
            onDismiss = { showPrivacyDialog = false }
        )
    }
}

@Composable
private fun PrivacyPolicyDialog(
    context: Context,
    onDismiss: () -> Unit
) {
    val lines = remember {
        try {
            context.resources.openRawResource(R.raw.privacy_policy)
                .bufferedReader()
                .use { it.readLines() }
        } catch (_: Exception) {
            listOf("Privacy Policy could not be loaded.")
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.82f),
            shape = RoundedCornerShape(20.dp),
            color = AppColors.Surface,
            border = BorderStroke(1.dp, AppColors.SurfaceBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.privacy_policy),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AppColors.TextSecondary
                        )
                    }
                }

                HorizontalDivider(color = AppColors.SurfaceBorder)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    lines.forEach { rawLine ->
                        val line = rawLine.trim()
                        when {
                            line.isEmpty() -> Spacer(modifier = Modifier.height(2.dp))
                            line.startsWith("# ") -> Text(
                                text = line.removePrefix("# "),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppColors.TextPrimary
                            )
                            line.startsWith("## ") -> Text(
                                text = line.removePrefix("## "),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.AccentEmerald,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            line.startsWith("- ") -> Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "• ",
                                    color = AppColors.AccentCoral,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = parseInlineMarkdown(line.removePrefix("- ")),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.TextSecondary,
                                    lineHeight = 19.sp
                                )
                            }
                            else -> Text(
                                text = parseInlineMarkdown(line),
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.TextSecondary,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = AppColors.SurfaceBorder)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.AccentCoral,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Đóng",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun parseInlineMarkdown(text: String): AnnotatedString {
    val tokenRegex = Regex("""(\*\*.*?\*\*|`.*?`)""")
    return buildAnnotatedString {
        var lastIndex = 0
        for (match in tokenRegex.findAll(text)) {
            if (match.range.first > lastIndex) {
                append(text.substring(lastIndex, match.range.first))
            }
            val token = match.value
            if (token.startsWith("**") && token.endsWith("**")) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)) {
                    append(token.substring(2, token.length - 2))
                }
            } else if (token.startsWith("`") && token.endsWith("`")) {
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace, color = AppColors.AccentBlue)) {
                    append(token.substring(1, token.length - 1))
                }
            }
            lastIndex = match.range.last + 1
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

@Composable
private fun PolicyItem(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = "• $title",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.AccentEmerald
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextSecondary,
            modifier = Modifier.padding(start = 12.dp, top = 2.dp)
        )
    }
}
