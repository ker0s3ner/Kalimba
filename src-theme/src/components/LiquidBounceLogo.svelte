<script lang="ts">
    // Kept under its old name so existing imports keep working; draws the Kalimba logo.
    export let width = "261.263px";
    export let height = "98px";
    export let badgeFill = "var(--accent-color)";
    export let badgeTextFill = "var(--text-color)";
    export let badgeGroupClass = "";
</script>

<svg
    class="kalimba-logo"
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 261.263 98"
    aria-hidden="true"
    style={`width: ${width}; height: ${height};`}
>
    <defs>
        <slot name="defs" />
    </defs>

    <g transform="translate(6 4)">
        <rect x="0" y="0" width="64" height="90" rx="18" fill="#8a5a2b" stroke="#c98a45" stroke-width="3" />
        {#each [0, 1, 2, 3, 4, 5, 6] as i}
            <rect x={9 + i * 6.8} y={8 + Math.abs(3 - i) * 5} width="4" height={38 - Math.abs(3 - i) * 5}
                  rx="2" fill="#f3e6d0" />
        {/each}
        <rect x="5" y="48" width="54" height="5" rx="2.5" fill="#4fb3a9" />
        <circle cx="32" cy="70" r="8" fill="#1e130b" />
    </g>

    <text x="86" y="58" font-family="Georgia, 'Times New Roman', serif" font-weight="700" font-size="52"
          fill="#fff7e6" stroke="#3a2412" stroke-width="5" paint-order="stroke" stroke-linejoin="round">Kalimba</text>

    <g class={badgeGroupClass}>
        <rect x="88" y="68" width="98" height="20" rx="10" fill={badgeFill} />
        <text x="137" y="82.5" text-anchor="middle" font-family="Inter, sans-serif" font-weight="700"
              font-size="11" letter-spacing="1.2" fill={badgeTextFill} stroke="#3a2412" stroke-width="3" paint-order="stroke">ISLAND EDITION</text>
    </g>
</svg>

<style>
    .kalimba-logo {
        display: block;
        flex: 0 0 auto;
        overflow: visible;
    }
</style>
