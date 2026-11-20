<script lang="ts">
    import type {GroupedModules, Module} from "../../integration/types";
    import ModuleCard from "./ModuleCard.svelte";
    import KalimbaMark from "../../components/KalimbaMark.svelte";
    import {fade} from "svelte/transition";
    import {onMount} from "svelte";
    import {getCategories, getModules} from "../../integration/rest";
    import {groupByCategory} from "../../integration/util";
    import {listen} from "../../integration/ws";
    import type {ModuleToggleEvent} from "../../integration/events";
    import ScaledClickGuiContent from "./ScaledClickGuiContent.svelte";
    import {setItem} from "../../integration/persistent_storage";

    const FAVORITES_KEY = "clickgui.favorites";
    const CATEGORY_KEY = "clickgui.category";
    const FAVORITES = "Favorites";

    let categories = $state<GroupedModules>({});
    let modules = $state<Module[]>([]);
    let icons = $state<Record<string, string>>({});
    let favorites = $state<string[]>([]);
    let active = $state(FAVORITES);
    let query = $state("");

    onMount(async () => {
        try {
            favorites = JSON.parse(localStorage.getItem(FAVORITES_KEY) ?? "[]");
            active = localStorage.getItem(CATEGORY_KEY) ?? FAVORITES;
        } catch {
            favorites = [];
        }

        modules = await getModules();
        categories = groupByCategory(modules);
        icons = Object.fromEntries(
            (await getCategories())
                .filter(category => category.icon)
                .map(category => [category.name, category.icon!])
        );

        if (active !== FAVORITES && !categories[active]) {
            active = Object.keys(categories)[0] ?? FAVORITES;
        }
    });

    listen("moduleToggle", (e: ModuleToggleEvent) => {
        const mod = modules.find(m => m.name === e.moduleName);
        if (mod) {
            mod.enabled = e.enabled;
        }
    });

    let visible = $derived.by(() => {
        const q = query.trim().toLowerCase();
        const pool = q
            ? modules
            : active === FAVORITES
                ? modules.filter(m => favorites.includes(m.name))
                : (categories[active] ?? []);

        return pool
            .filter(m => !q
                || m.name.toLowerCase().includes(q)
                || m.description.toLowerCase().includes(q)
                || m.aliases.some(a => a.toLowerCase().includes(q)))
            .toSorted((a, b) => a.name.localeCompare(b.name));
    });

    function select(name: string) {
        active = name;
        query = "";
        setItem(CATEGORY_KEY, name);
    }

    function toggleFavorite(name: string) {
        favorites = favorites.includes(name)
            ? favorites.filter(f => f !== name)
            : [...favorites, name];
        setItem(FAVORITES_KEY, JSON.stringify(favorites));
    }

    function showFallbackIcon(event: Event) {
        (event.currentTarget as HTMLImageElement).src = "img/clickgui/icon-client.svg";
    }
</script>

<ScaledClickGuiContent>
    <div class="clickgui" transition:fade|global={{duration: 200}}>
        <div class="window">
            <aside class="sidebar">
                <div class="brand">
                    <KalimbaMark size={30}/>
                    <div>
                        <div class="brand-name">Kalimba</div>
                        <div class="brand-sub">Island edition</div>
                    </div>
                </div>

                <div class="section-label">MODULES</div>
                <button class="nav" class:active={active === FAVORITES && !query} on:click={() => select(FAVORITES)}>
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor"
                         stroke-width="1.6" stroke-linejoin="round">
                        <path d="M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1L3.2 9.5l6.1-.9z"/>
                    </svg>
                    Favorites
                </button>
                {#each Object.keys(categories) as category (category)}
                    <button class="nav" class:active={active === category && !query} on:click={() => select(category)}>
                        <img
                                src={icons[category] ?? `img/clickgui/icon-${category.toLowerCase()}.svg`}
                                alt=""
                                on:error={showFallbackIcon}
                        />
                        {category}
                    </button>
                {/each}
            </aside>

            <main class="content">
                <label class="search">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor"
                         stroke-width="1.8" stroke-linecap="round">
                        <circle cx="11" cy="11" r="6.5"/>
                        <path d="M16 16l4.5 4.5"/>
                    </svg>
                    <input type="text" placeholder="Search modules..." bind:value={query} spellcheck="false"/>
                </label>

                <div class="grid">
                    {#each visible as mod (mod.name)}
                        <ModuleCard
                                name={mod.name}
                                enabled={mod.enabled}
                                description={mod.description}
                                favorite={favorites.includes(mod.name)}
                                onFavorite={() => toggleFavorite(mod.name)}
                        />
                    {:else}
                        <div class="empty">
                            {active === FAVORITES && !query ? "Star a module to pin it here." : "No modules found."}
                        </div>
                    {/each}
                </div>
            </main>
        </div>

        <div class="hints">Left click opens settings &middot; Switch toggles &middot; Star pins a module</div>
    </div>
</ScaledClickGuiContent>

<style lang="scss">
  .clickgui {
    position: absolute;
    inset: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  .window {
    display: flex;
    width: min(880px, 92%);
    height: min(520px, 82%);
    border-radius: 18px;
    overflow: hidden;
    background:
            linear-gradient(180deg, color-mix(in srgb, #2b1b10 88%, transparent), color-mix(in srgb, #1e130b 92%, transparent));
    border: 1px solid color-mix(in srgb, var(--accent-color) 35%, transparent);
    box-shadow: 0 20px 60px color-mix(in srgb, black 55%, transparent),
    inset 0 1px 0 color-mix(in srgb, #fff 8%, transparent);
    backdrop-filter: blur(14px);
  }

  .sidebar {
    width: 190px;
    flex-shrink: 0;
    padding: 16px 12px;
    display: flex;
    flex-direction: column;
    gap: 3px;
    background: color-mix(in srgb, black 22%, transparent);
    border-right: 1px solid color-mix(in srgb, var(--accent-color) 18%, transparent);
    overflow-y: auto;
  }

  .brand {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 2px 6px 14px;
  }

  .brand-name {
    color: var(--text-color);
    font-weight: 700;
    font-size: 15px;
  }

  .brand-sub {
    color: var(--sea-color, #4fb3a9);
    font-size: 10px;
    letter-spacing: .05em;
  }

  .section-label {
    color: var(--text-dimmed-color);
    font-size: 10px;
    letter-spacing: .12em;
    padding: 6px 8px;
    opacity: .7;
  }

  .nav {
    all: unset;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 10px;
    border-radius: 8px;
    cursor: pointer;
    font-size: 13px;
    color: var(--text-dimmed-color);
    transition: background-color .2s, color .2s;

    img {
      width: 16px;
      height: 16px;
      filter: invert(78%) sepia(20%) saturate(500%) hue-rotate(350deg);
    }

    &:hover {
      color: var(--text-color);
      background: color-mix(in srgb, var(--accent-color) 10%, transparent);
    }

    &.active {
      color: var(--text-color);
      background: color-mix(in srgb, var(--accent-color) 18%, transparent);
      box-shadow: inset 2px 0 0 var(--accent-color);
    }
  }

  .content {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    padding: 14px 16px;
    gap: 12px;
  }

  .search {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 9px 14px;
    border-radius: 999px;
    color: var(--text-dimmed-color);
    background: color-mix(in srgb, black 25%, transparent);
    border: 1px solid color-mix(in srgb, var(--wood-light, #8a5a2b) 30%, transparent);

    &:focus-within {
      border-color: var(--sea-color, #4fb3a9);
    }

    input {
      all: unset;
      flex: 1;
      color: var(--text-color);
      font-size: 13px;
    }
  }

  .grid {
    flex: 1;
    overflow-y: auto;
    display: grid;
    grid-template-columns: 1fr 1fr;
    align-content: start;
    gap: 10px;
    padding-right: 6px;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: color-mix(in srgb, var(--accent-color) 45%, transparent);
      border-radius: 2px;
    }
  }

  .empty {
    grid-column: 1 / -1;
    text-align: center;
    color: var(--text-dimmed-color);
    padding: 40px 0;
    font-size: 13px;
  }

  .hints {
    color: color-mix(in srgb, var(--text-color) 70%, transparent);
    font-size: 11px;
    text-shadow: 0 1px 4px black;
  }
</style>
