package co.edu.uniquindio.entorno.core.component

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.entorno.core.theme.EntornoTheme
import co.edu.uniquindio.entorno.core.util.toRelativeTime
import co.edu.uniquindio.entorno.domain.model.Comment
import co.edu.uniquindio.entorno.features.report.SampleComments

private val AvatarShape = RoundedCornerShape(12.dp)

// TODO: tomar el rol del autor del modelo cuando Comment lo incluya
@Composable
fun CommentItem(
    comment: Comment,
    isCurrentUser: Boolean = false,
    isModerator: Boolean = false,
    usefulCount: Int = 0,
    onReply: () -> Unit = {},
    onUseful: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val containerColor = if (isModerator) colors.primaryContainer else colors.surface
    val borderColor = if (isModerator) colors.primary else colors.outlineVariant
    val bodyColor = if (isModerator) colors.onPrimaryContainer else colors.onSurface
    val nameColor = if (isModerator || isCurrentUser) colors.onPrimaryContainer else colors.onSurface
    val displayName = if (isCurrentUser) "Tú · ${comment.authorName}" else comment.authorName

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CommentAvatar(
                name = comment.authorName,
                highlighted = isCurrentUser || isModerator,
                showShield = isModerator
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = nameColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isModerator) ModeratorTag()
                }
                Text(
                    text = comment.createdAt.toRelativeTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isModerator) colors.onPrimaryContainer else colors.onSurfaceVariant
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = comment.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = bodyColor
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CommentAction(text = "Útil · $usefulCount", onClick = onUseful)
                    CommentAction(text = "Responder", onClick = onReply)
                }
            }
        }
    }
}

@Composable
private fun CommentAction(text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
    Spacer(Modifier.width(8.dp))
}

@Composable
private fun CommentAvatar(name: String, highlighted: Boolean, showShield: Boolean) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.size(40.dp),
        shape = AvatarShape,
        color = if (highlighted) colors.primary else colors.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (showShield) {
                // TODO: reemplazar por el icono de escudo del mockup
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = name.initials(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (highlighted) colors.onPrimary else colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ModeratorTag() {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TODO: reemplazar por el icono de escudo del mockup
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "MODERADOR",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1
            )
        }
    }
}

private fun String.initials(): String =
    trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

@Preview(showBackground = true)
@Composable
private fun CommentItemNormalPreview() {
    EntornoTheme {
        CommentItem(
            comment = SampleComments.comments[0],
            usefulCount = 3,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CommentItemModeratorPreview() {
    EntornoTheme {
        CommentItem(
            comment = SampleComments.comments[1],
            isModerator = true,
            usefulCount = 12,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CommentItemCurrentUserPreview() {
    EntornoTheme {
        CommentItem(
            comment = SampleComments.comments[2],
            isCurrentUser = true,
            usefulCount = 1,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CommentItemModeratorDarkPreview() {
    EntornoTheme {
        CommentItem(
            comment = SampleComments.comments[1],
            isModerator = true,
            usefulCount = 12,
            modifier = Modifier.padding(16.dp)
        )
    }
}
