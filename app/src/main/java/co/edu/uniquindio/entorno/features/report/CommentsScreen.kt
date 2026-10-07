package co.edu.uniquindio.entorno.features.report

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.entorno.R
import co.edu.uniquindio.entorno.core.component.CommentItem
import co.edu.uniquindio.entorno.core.component.icon
import co.edu.uniquindio.entorno.core.component.labelForCitizen
import co.edu.uniquindio.entorno.core.theme.EntornoTheme
import co.edu.uniquindio.entorno.core.theme.categoryColors
import co.edu.uniquindio.entorno.domain.model.Comment
import co.edu.uniquindio.entorno.domain.model.Report
import co.edu.uniquindio.entorno.features.home.SampleReports

@Composable
fun CommentsScreen(
    report: Report,
    comments: List<Comment>,
    currentUserId: String,
    moderatorIds: Set<String> = emptySet(),
    onBack: () -> Unit = {},
    onSend: (String) -> Unit = {},
    onViewReport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // "Más recientes": el comentario más nuevo va primero.
    val sortedComments = remember(comments) { comments.sortedByDescending { it.createdAt } }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            CommentsHeader(total = comments.size, onBack = onBack)

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ReportContextCard(report = report, onViewReport = onViewReport)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Conversación",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Más recientes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (sortedComments.isEmpty()) {
                EmptyComments(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = sortedComments, key = { it.id }) { comment ->
                        CommentItem(
                            comment = comment,
                            isCurrentUser = comment.authorId == currentUserId,
                            isModerator = comment.authorId in moderatorIds
                        )
                    }
                }
            }

            CommentInputBar(onSend = onSend)
        }
    }
}

@Composable
private fun CommentsHeader(total: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Column {
            Text(
                text = "Comentarios",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$total en total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReportContextCard(report: Report, onViewReport: () -> Unit) {
    val colors = categoryColors(report.category)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = colors.chipBackground
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = report.category.icon(),
                        contentDescription = null,
                        tint = colors.icon,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${report.category.label} · ${report.status.labelForCitizen()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Ver",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable(onClick = onViewReport)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun EmptyComments(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_comment),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = "Todavía no hay comentarios",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = "Sé el primero en aportar algo sobre este reporte.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CommentInputBar(onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un comentario…") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )
            FilledIconButton(
                onClick = {
                    onSend(text.trim())
                    text = ""
                },
                enabled = text.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar comentario"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CommentsScreenPreview() {
    EntornoTheme {
        CommentsScreen(
            report = SampleReports.reports[0],
            comments = SampleComments.comments,
            currentUserId = SampleComments.CURRENT_USER_ID,
            moderatorIds = SampleComments.moderatorIds
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CommentsScreenEmptyPreview() {
    EntornoTheme {
        CommentsScreen(
            report = SampleReports.reports[0],
            comments = emptyList(),
            currentUserId = SampleComments.CURRENT_USER_ID
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CommentsScreenDarkPreview() {
    EntornoTheme {
        CommentsScreen(
            report = SampleReports.reports[0],
            comments = SampleComments.comments,
            currentUserId = SampleComments.CURRENT_USER_ID,
            moderatorIds = SampleComments.moderatorIds
        )
    }
}
