/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.eros.main.profiles

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.observer.map.MapChange.Companion.values
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.TranslatableComponents
import de.bixilon.minosoft.data.text.events.click.OpenFileClickEvent
import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.gui.eros.dialog.SimpleErosConfirmationDialog
import de.bixilon.minosoft.gui.eros.dialog.profiles.ProfileCreateDialog
import de.bixilon.minosoft.gui.eros.main.InfoPane
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.util.DesktopUtil
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.delegate.JavaFXDelegate.observeBiMapFX
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.layout.Pane


class ProfilesListController : EmbeddedJavaFXController<Pane>() {
    @FXML private lateinit var profilesListViewFX: ListView<Profile>
    @FXML private lateinit var profileInfoFX: InfoPane<Profile>

    @FXML private lateinit var createProfileButtonFX: Button

    var profileManager: ProfileManager<Profile>? = null
        set(value) {
            check(value != null)
            field = value
        }

    override fun init() {
        profilesListViewFX.setCellFactory {
            val controller = ProfileCardController.build()
            controller.profileList = this

            controller.root.setOnMouseClicked {
                if (it.clickCount != 2) {
                    return@setOnMouseClicked
                }
                val item = controller.item ?: return@setOnMouseClicked
                profileManager?.selected = item
                setProfileInfo(item)
            }
            return@setCellFactory controller
        }

        profilesListViewFX.selectionModel.selectedItemProperty().addListener { _, _, new ->
            setProfileInfo(new)
        }
        createProfileButtonFX.ctext = CREATE
    }

    override fun postInit() {
        root.setOnKeyPressed { profilesListViewFX.selectionModel.select(null) } // ToDo: Only on escape; not working
    }

    fun initWatch() {
        profileManager!!::profiles.observeBiMapFX(this) {
            profilesListViewFX.items -= it.removes.values()
            profilesListViewFX.items += it.adds.values()
            profilesListViewFX.refresh()
        }
    }

    @FXML
    fun refreshList() {
        if (profileManager == null) {
            return
        }
        val selected = profilesListViewFX.selectionModel.selectedItem
        profilesListViewFX.items.clear()

        for (profile in profileManager!!.profiles.values) {
            updateProfile(profile)
        }

        profilesListViewFX.items.contains(selected).decide(selected, null).let {
            profilesListViewFX.selectionModel.select(it)

            profilesListViewFX.scrollTo(it)
        }
    }

    private fun updateProfile(profile: Profile) {
        val wasSelected = profilesListViewFX.selectionModel.selectedItem === profile
        // Platform.runLater {serverListViewFX.items.remove(card)}


        if (!profilesListViewFX.items.contains(profile)) {
            profilesListViewFX.items.add(profile)
        }


        if (wasSelected) {
            profilesListViewFX.selectionModel.select(profile)
        }
    }


    private fun setProfileInfo(profile: Profile?) {
        val profileManager = this.profileManager
        if (profile == null) {
            profileInfoFX.reset()
            return
        }
        profileInfoFX.update(profile, PROFILE_INFO_PROPERTIES, actions = arrayOf(
            Button("Delete").apply {
                isDisable = !profile.manager.canDelete(profile)
                setOnAction {
                    SimpleErosConfirmationDialog(confirmButtonText = "minosoft:general.delete".toResourceLocation(), onConfirm = {
                        profile.manager.deleteAsync(profile)
                        JavaFXUtil.runLater {
                            profilesListViewFX.items.remove(profile)
                            setProfileInfo(profilesListViewFX.selectionModel.selectedItem)
                        }
                    }).show()
                }
                ctext = TranslatableComponents.GENERAL_DELETE
            },
            Button("Edit").apply {
                // ToDo: proper profile editing
                setOnAction { DefaultThreadPool += { DesktopUtil.openFile(profile.manager.getPath(profile.name)) } }
                ctext = EDIT
            },
            Button("Set primary").apply {
                isDisable = profile.manager.selected == profile
                setOnAction {
                    profile.manager.selected = profile
                    isDisable = true
                }
                ctext = SET_PRIMARY
            }
        ))
    }


    @FXML
    fun createProfile() {
        val profileManager = profileManager ?: return
        ProfileCreateDialog(profileManager, false) { manager, profile ->
            if (manager !== profileManager) {
                return@ProfileCreateDialog
            }
            updateProfile(profile)
        }.show()
    }


    companion object {
        val LAYOUT = "minosoft:eros/main/profiles/profiles_list.fxml".toResourceLocation()

        private val EDIT = "minosoft:profiles.profile.list.button.edit".toResourceLocation()
        private val SET_PRIMARY = "minosoft:profiles.profile.list.button.set_primary".toResourceLocation()
        private val CREATE = "minosoft:profiles.profile.list.button.create".toResourceLocation()

        private val PROFILE_INFO_PROPERTIES: List<Pair<ResourceLocation, (Profile) -> Any?>> = listOf(
            "minosoft:profiles.profile.name".toResourceLocation() to { it.name },
            "minosoft:profiles.profile.description".toResourceLocation() to { it.description },

            TranslatableComponents.GENERAL_EMPTY to { " " },

            "minosoft:profiles.profile.disk_path".toResourceLocation() to {
                val path = it.manager.getPath(it.name)
                TextComponent(path, clickEvent = OpenFileClickEvent(path))
            },
        )
    }
}
