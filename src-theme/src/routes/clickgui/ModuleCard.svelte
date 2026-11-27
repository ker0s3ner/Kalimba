<script lang="ts">
    import {slide} from "svelte/transition";
    import {quintOut} from "svelte/easing";
    import {getModuleSettings, setModuleEnabled, setModuleSettings} from "../../integration/rest";
    import type {ConfigurableSetting} from "../../integration/types";
    import GenericSetting from "./setting/common/GenericSetting.svelte";
    import {convertToSpacedString, spaceSeperatedNames} from "../../theme/theme_config";

    export let name: string;
    export let enabled: boolean;
    export let description: string;
    export let favorite = false;
    export let onFavorite: () => void = () => {};

    const path = `clickgui.${name}`;
    let expanded = false;
    let configurable: ConfigurableSetting | undefined;
    let hasSettings = true;

    async function loadSettings() {
        configurable = await getModuleSettings(name);
        hasSettings = configurable.value.filter(v => v.name !== "Bind" && v.name !== "Hidden").length > 0;
    }

    async function toggleExpanded() {
        if (!expanded && !configurable) {
            await loadSettings();
        }
        expanded = !expanded;
    }

    async function updateSettings() {
        if (!configurable) return;
        await setModuleSettings(name, configurable);
        await loadSettings();
    }

    async function toggle(event: Event) {
        event.stopPropagation();
        await setModuleEnabled(name, !enabled);
    }

    function star(event: Event) {
        event.stopPropagation();
        onFavorite();
    }
</script>

<div class="card" class:enabled class:expanded>
    <!-- svelte-ignore a11y-click-events-have-key-events -->
    <!-- svelte-ignore a11y-no-static-element-interactions -->
    <div class="row" on:click={toggleExpanded} on:contextmenu|preventDefault={toggleExpanded}>
        <div class="text">
            <div class="title">{$spaceSeperatedNames ? convertToSpacedString(name) : name}</div>
            <div class="desc" title={description}>{description}</div>
        </div>

        <button class="star" class:on={favorite} aria-label="Favorite" on:click={star}>
            <svg viewBox="0 0 24 24" width="16" height="16" fill={favorite ? "currentColor" : "none"}
                 stroke="currentColor" stroke-width="1.6" stroke-linejoin="round">
                <path d="M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1L3.2 9.5l6.1-.9z"/>
            </svg>
        </button>

        <button class="switch" class:on={enabled} aria-label="Toggle module" aria-pressed={enabled}
                on:click={toggle}>
            <span class="knob"></span>
        </button>
    </div>

    {#if expanded}
        <div class="settings" transition:slide={{duration: 300, easing: quintOut}}>
            {#if configurable}
                {#each configurable.value as setting (setting.name)}
                    <GenericSetting {path} bind:setting on:change={updateSettings}/>
                {/each}
                {#if !hasSettings}
                    <div class="none">No settings</div>
                {/if}
            {/if}
        </div>
    {/if}
</div>

<style lang="scss">
  .card {
    background: color-mix(in srgb, var(--surface-color) 62%, transparent);
    border: 1px solid color-mix(in srgb, var(--wood-light, #8a5a2b) 28%, transparent);
    border-radius: 10px;
    transition: border-color .2s, background-color .2s;

    &:hover {
      border-color: color-mix(in srgb, var(--accent-color) 55%, transparent);
    }

    &.enabled {
      border-color: color-mix(in srgb, var(--sea-color, #4fb3a9) 55%, transparent);
    }
  }

  .row {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    cursor: pointer;
  }

  .text {
    flex: 1;
    min-width: 0;
  }

  .title {
    color: var(--text-color);
    font-size: 13px;
    font-weight: 600;
  }

  .desc {
    color: var(--text-dimmed-color);
    font-size: 11px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    margin-top: 2px;
  }

  .star {
    all: unset;
    cursor: pointer;
    display: flex;
    color: var(--text-dimmed-color);
    transition: color .2s;

    &:hover, &.on {
      color: var(--premium-color);
    }
  }

  .switch {
    all: unset;
    cursor: pointer;
    width: 34px;
    height: 18px;
    border-radius: 9px;
    background: color-mix(in srgb, var(--text-dimmed-color) 30%, transparent);
    position: relative;
    transition: background-color .25s;

    .knob {
      position: absolute;
      top: 2px;
      left: 2px;
      width: 14px;
      height: 14px;
      border-radius: 50%;
      background: var(--text-color);
      transition: transform .25s cubic-bezier(.3, 1.4, .5, 1);
    }

    &.on {
      background: var(--sea-color, #4fb3a9);

      .knob {
        transform: translateX(16px);
      }
    }
  }

  .settings {
    border-top: 1px solid color-mix(in srgb, var(--wood-light, #8a5a2b) 28%, transparent);
    padding: 4px 12px 8px;
    background: color-mix(in srgb, black 18%, transparent);
    border-radius: 0 0 10px 10px;
  }

  .none {
    color: var(--text-dimmed-color);
    font-size: 11px;
    padding: 8px 0;
  }
</style>
