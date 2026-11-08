package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.R
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.theme.AppColors

@Composable
fun TermsOfServiceScreen(
    onAccepted: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val privacyUrl = "https://sites.google.com/view/dzungvpn/home"

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
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.AccentCoral)
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
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.SurfaceBorder),
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
            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, privacyUrl.toUri())
                context.startActivity(intent)
            }) {
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
