package dev.jvfl.progtv.domain.model

/**
 * pt-BR labels for iptv-org category ids (https://iptv-org.github.io/api/categories.json).
 * Grouping still uses the original id; only the displayed name is localized.
 */
object CategoryLabels {
    private val PT_BR = mapOf(
        "auto" to "Automotivo",
        "animation" to "Animação",
        "business" to "Negócios",
        "classic" to "Clássicos",
        "comedy" to "Comédia",
        "cooking" to "Culinária",
        "culture" to "Cultura",
        "documentary" to "Documentário",
        "education" to "Educação",
        "entertainment" to "Entretenimento",
        "family" to "Família",
        "general" to "Geral",
        "interactive" to "Interativo",
        "kids" to "Infantil",
        "legislative" to "Legislativo",
        "lifestyle" to "Estilo de Vida",
        "movies" to "Filmes",
        "music" to "Música",
        "news" to "Notícias",
        "outdoor" to "Aventura",
        "public" to "Público",
        "relax" to "Relax",
        "religious" to "Religioso",
        "series" to "Séries",
        "science" to "Ciência",
        "shop" to "Compras",
        "sports" to "Esportes",
        "travel" to "Viagem",
        "weather" to "Clima",
        "xxx" to "Adulto",
    )

    /** Returns the pt-BR label for a category id, capitalizing unknown ids as a fallback. */
    fun localize(id: String): String {
        PT_BR[id.lowercase()]?.let { return it }
        return id.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
