package com.willfp.eco.internal.gui.menu

import com.willfp.eco.core.gui.component.GUIComponent
import com.willfp.eco.core.gui.menu.CloseHandler
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.menu.OpenHandler
import com.willfp.eco.core.gui.menu.MenuEvent
import com.willfp.eco.core.gui.menu.MenuEventHandler
import com.willfp.eco.core.gui.slot.FillerSlot
import com.willfp.eco.core.gui.slot.Slot
import com.willfp.eco.util.NamespacedKeyUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@Suppress("UNCHECKED_CAST")
class EcoMenu(
    private val rows: Int,
    private val columns: Int,
    private val componentsAtPoints: Map<GUIPosition, List<OffsetComponent>>,
    private val title: String,
    private val onClose: List<CloseHandler>,
    private val onRender: List<(Player, Menu) -> Unit>,
    private val onOpen: List<OpenHandler>,
    private val menuEventHandlers: List<MenuEventHandler<*>>
) : Menu {
    private fun getPossiblyReactiveSlot(row: Int, column: Int, player: Player?, menu: Menu?): Slot {
        if (row < 1 || row > this.rows || column < 1 || column > this.columns) {
            return emptyFillerSlot
        }

        val guiPosition = GUIPosition(row, column)
        val components = componentsAtPoints[guiPosition] ?: return emptyFillerSlot

        for (component in components) {
            val found = if (player != null && menu != null) component.component.getSlotAt(
                component.rowOffset,
                component.columnOffset,
                player,
                menu
            ) else component.component.getSlotAt(
                component.rowOffset,
                component.columnOffset
            )

            if (found != null) {
                return found
            }
        }

        return emptyFillerSlot
    }

    override fun getSlot(row: Int, column: Int): Slot =
        getPossiblyReactiveSlot(row, column, null, null)

    override fun getSlot(row: Int, column: Int, player: Player, menu: Menu): Slot =
        getPossiblyReactiveSlot(row, column, player, menu)

    override fun open(player: Player): Inventory {
        val inventory = if (columns == 9) {
            Bukkit.createInventory(null, rows * columns, title)
        } else {
            Bukkit.createInventory(null, InventoryType.DISPENSER, title)
        }

        player.forceMenuOpen(this)

        MenuHandler.registerInventory(inventory, this, player)

        inventory.asRenderedInventory()?.render()

        player.openInventory(inventory)

        onOpen.forEach { it.handle(player, this) }

        player.stopForceMenuOpen()
        return inventory
    }

    fun handleClose(event: InventoryCloseEvent) {
        onClose.forEach { it.handle(event, this) }
        event.inventory.asRenderedInventory()?.render()
        MenuHandler.unregisterInventory(event.inventory)
    }

    override fun getRows() = rows

    override fun getColumns() = columns

    override fun getTitle() = title

    override fun getCaptiveItems(player: Player): List<ItemStack> {
        val inventory = player.openInventory.topInventory.asRenderedInventory() ?: return emptyList()
        return inventory.captiveItems.values.toList()
    }

    override fun getCaptiveItem(player: Player, row: Int, column: Int): ItemStack? {
        if (row < 1 || row > this.rows || column < 1 || column > this.columns) {
            return null
        }

        val inventory = player.openInventory.topInventory.asRenderedInventory() ?: return null
        return inventory.captiveItems[GUIPosition(row, column)]
    }

    override fun callEvent(player: Player, event: MenuEvent) {
        for (handler in menuEventHandlers) {
            if (handler.canHandleEvent(event)) {
                handler.handle(event, player)
            }
        }
    }

    private fun <T : MenuEvent> MenuEventHandler<T>.handle(event: MenuEvent, player: Player) {
        this.handle(player, this@EcoMenu, event as T)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("addState(player, key.toString(), value)"))
    override fun <T : Any, Z : Any> writeData(
        player: Player,
        key: NamespacedKey,
        type: PersistentDataType<T, Z>,
        value: Z
    ) = addState(player, key.toString(), value)

    @Deprecated("Deprecated in Java", ReplaceWith("getState(player, key.toString())"))
    override fun <T : Any, Z : Any> readData(player: Player, key: NamespacedKey, type: PersistentDataType<T, Z>): T? =
        getState(player, key.toString())

    @Deprecated("Deprecated in Java")
    override fun getKeys(player: Player): Set<NamespacedKey> {
        val inventory = player.openInventory.topInventory.asRenderedInventory() ?: return emptySet()
        return inventory.state.keys.mapNotNull { NamespacedKeyUtils.fromStringOrNull(it) }.toSet()
    }

    override fun addState(player: Player, key: String, value: Any?) {
        val inventory = player.openInventory.topInventory.asRenderedInventory() ?: return
        inventory.state[key] = value
    }

    override fun getState(player: Player): Map<String, Any?> {
        val inventory = player.openInventory.topInventory.asRenderedInventory() ?: return emptyMap()
        return inventory.state.toMap()
    }

    override fun <T : Any> getState(player: Player, key: String): T? {
        val inventory = player.openInventory.topInventory.asRenderedInventory() ?: return null
        return inventory.state[key] as? T?
    }

    override fun removeState(player: Player, key: String) {
        val inventory = player.openInventory.topInventory.asRenderedInventory() ?: return
        inventory.state.remove(key)
    }

    override fun clearState(player: Player) {
        val inventory = player.openInventory.topInventory.asRenderedInventory() ?: return
        inventory.state.clear()
    }

    override fun refresh(player: Player) {
        player.openInventory.topInventory.asRenderedInventory()?.render()
    }

    fun runOnRender(player: Player) =
        onRender.forEach { it(player, this) }
}

data class OffsetComponent(
    val component: GUIComponent,
    val rowOffset: Int,
    val columnOffset: Int
)

data class GUIPosition(
    val row: Int,
    val column: Int
)

val emptyFillerSlot = FillerSlot(ItemStack(Material.AIR))
