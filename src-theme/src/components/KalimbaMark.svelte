<script lang="ts">
    export let size = 32;
</script>

<!-- Kalimba thumb piano: wooden body with sound hole and metal tines -->
<svg viewBox="0 0 64 64" width={size} height={size} aria-hidden="true">
    <rect x="9" y="6" width="46" height="52" rx="12" fill="#8a5a2b"/>
    <rect x="9" y="6" width="46" height="52" rx="12" fill="none" stroke="#c98a45" stroke-width="2"/>
    {#each [0, 1, 2, 3, 4, 5, 6] as i}
        <rect x={16 + i * 5.2} y={10 + Math.abs(3 - i) * 3} width="3" height={26 - Math.abs(3 - i) * 3}
              rx="1.5" fill="#f3e6d0"/>
    {/each}
    <rect x="13" y="36" width="38" height="3" rx="1.5" fill="#4fb3a9"/>
    <circle cx="32" cy="48" r="5" fill="#1e130b"/>
</svg>
