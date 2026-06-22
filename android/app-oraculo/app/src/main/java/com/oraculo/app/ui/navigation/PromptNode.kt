
package com.oraculo.app.ui.navigation

/*
 * Nodo del menú lateral de prompts.
 *
 * title:
 * Texto visible en el drawer.
 *
 * children:
 * Lista de subtemas.
 *
 * Regla V1.5:
 * - Si no tiene hijos, un click envía title como prompt.
 * - Si tiene hijos, un click expande/contrae.
 * - Si tiene hijos, doble click envía title como prompt.
 */
data class PromptNode(
    val title: String,
    val children: List<PromptNode> = emptyList()
) {
    val hasChildren: Boolean
        get() = children.isNotEmpty()
}

