<script lang="ts">
    import {onMount, tick} from "svelte";
    import type {Module} from "../../../integration/types";
    import {getModules} from "../../../integration/rest";
    import {listen} from "../../../integration/ws";
    import {getTextWidth} from "../../../integration/text_measurement";
    import {flip} from "svelte/animate";
    import {fly} from "svelte/transition";
    import {convertToSpacedString, spaceSeperatedNames} from "../../../theme/theme_config";
    import type {HudArrayListSettings} from "../components";

    export let settings: { [name: string]: any };

    let cSettings = settings as HudArrayListSettings;

    let enabledModules: Module[] = [];

    async function updateEnabledModules() {
        const modules = await getModules();
        const visibleModules = modules.filter(m => m.enabled && !m.hidden);

        const modulesWithWidths = visibleModules.map(module => {
            const formattedName = $spaceSeperatedNames ? convertToSpacedString(module.name) : module.name;
            const fullName = module.tag == null || !cSettings.showTags
                ? formattedName
                : formattedName + " " + module.tag;

            return {
                ...module,
                width: getTextWidth(fullName, "500 14px Inter")
            };
        });

        modulesWithWidths.sort((a, b) => cSettings.order === "Ascending" ? a.width - b.width : b.width - a.width);

        enabledModules = modulesWithWidths;
        await tick();
    }

    $: if (cSettings !== settings) {
        cSettings = settings as HudArrayListSettings;
        updateEnabledModules();
    }

    spaceSeperatedNames.subscribe(async () => {
        await updateEnabledModules();
    });

    onMount(async () => {
        await updateEnabledModules();
    });

    listen("moduleToggle", async () => {
        await updateEnabledModules();
    });

    listen("refreshArrayList", async () => {
        await updateEnabledModules();
    });
</script>

<div class="arraylist">
    {#each enabledModules as {name, tag} (name)}
        <div
                class="module"
                style={cSettings.itemAlignment === "Left" ? "margin-right: auto;" : "margin-left: auto;"}
                animate:flip={{ duration: 200 }}
                transition:fly={{ x: 50, duration: 200 }}
        >
            {$spaceSeperatedNames ? convertToSpacedString(name) : name}
            {#if tag && cSettings.showTags}
                <span class="tag"> {tag}</span>
            {/if}
        </div>
    {/each}
</div>

<style lang="scss">

  /* Island style: no boxes, just sunlit text with a soft shadow, fading from sand to sea */
  .module {
    color: var(--arraylist-text-color);
    font-size: 14px;
    padding: 1px 4px;
    width: max-content;
    font-weight: 600;
    letter-spacing: .02em;
    text-shadow: 0 1px 6px color-mix(in srgb, black 70%, transparent);
    background: linear-gradient(90deg, transparent, color-mix(in srgb, var(--sea-color) 10%, transparent));
    border-right: solid 2px var(--arraylist-border-color);
  }

  .tag {
    color: var(--arraylist-tag-color);
    font-weight: 500;
  }
</style>
