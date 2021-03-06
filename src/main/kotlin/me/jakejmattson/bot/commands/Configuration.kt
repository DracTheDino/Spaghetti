package me.jakejmattson.bot.commands

import dev.kord.common.entity.Permission
import dev.kord.common.kColor
import me.jakejmattson.bot.conversations.configurationConversation
import me.jakejmattson.bot.data.Configuration
import me.jakejmattson.bot.extensions.requiredPermission
import me.jakejmattson.discordkt.api.arguments.ChannelArg
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.dsl.commands
import java.awt.Color

@Suppress("unused")
fun configurationCommands(configuration: Configuration) = commands("Configuration") {
    guildCommand("Configure") {
        requiredPermission = Permission.Administrator
        description = "Start a conversation in the chat to configure the bot for your server."
        execute {
            val guildConfiguration = configuration.guildConfigurations[guild.id.value]
            if (guildConfiguration != null) {
                respond("Server has already been configured. Type `${guildConfiguration.serverPrefix}setPrefix`, `${guildConfiguration.serverPrefix}setStaffReviewChannel` or `${guildConfiguration.serverPrefix}setPublicVotingChannel`")
                return@execute
            }
            configurationConversation(configuration, guild)
                .startPublicly(discord, author, channel)
            }
        }

    guildCommand("SetPrefix") {
        requiredPermission = Permission.Administrator
        description = "Change server prefix after initial configuration."
        execute(EveryArg) {
            val guildConfiguration = configuration.guildConfigurations[guild.id.value]
            if (guildConfiguration == null) {
                respond("Server has not been configured. Run `s!configure` first.")
                return@execute
            }
            guildConfiguration.serverPrefix = args.first
            respond("New server prefix: `${guildConfiguration.serverPrefix}`")
        }
    }

    guildCommand("setStaffReviewChannel") {
        requiredPermission = Permission.Administrator
        description = "Change staff review channel after initial configuration."
        execute(ChannelArg) {
            val guildConfiguration = configuration.guildConfigurations[guild.id.value]
            if (guildConfiguration == null) {
                respond("Server has not been configured. Run `s!configure` first.")
                return@execute
            }
            guildConfiguration.staffReviewChannel = args.first.id
            respond("New staff review channel: <#${guildConfiguration.staffReviewChannel.value}>")
        }
    }

    guildCommand("setPublicVotingChannel") {
        requiredPermission = Permission.Administrator
        description = "Change public voting channel after initial configuration."
        execute(ChannelArg) {
            val guildConfiguration = configuration.guildConfigurations[guild.id.value]
            if (guildConfiguration == null) {
                respond("Server has not been configured. Run `s!configure` first.")
                return@execute
            }
            guildConfiguration.publicVotingChannel = args.first.id
            respond("New public voting channel: <#${guildConfiguration.publicVotingChannel.value}>")
        }
    }

    guildCommand("ShowConfiguration") {
        requiredPermission = Permission.Administrator
        description = "Show the server's current configuration."
        execute {
            val prefix = "s!"
            val guildConfiguration = discord
                .getInjectionObjects(Configuration::class)
                .guildConfigurations
            respond {
                title = "Guild Configuration for Guild: ${guild.name}"
                color = Color.ORANGE.kColor
                field {
                    name = "Prefix"
                    value = "`${guild.let { guildConfiguration[it.id.value]?.serverPrefix } ?: prefix}`"
                }
                field {
                    name = "Staff Review Channel"
                    value = "<#${guild.let { guildConfiguration[it.id.value]?.staffReviewChannel?.asString }}>"
                }
                field {
                    name = "Public Voting Channel"
                    value = "<#${guild.let { guildConfiguration[it.id.value]?.publicVotingChannel?.asString }}>"
                }
            }
        }
    }
}