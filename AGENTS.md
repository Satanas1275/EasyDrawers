## Goal
- Develop a Fabric mod "EasyDrawers" with a directional drawer block that stores one block type, displays its texture + count on the face, and has a custom creative tab.

## Constraints & Preferences
- mod_id: `easydrawers`, author: `Satanas1275`, Minecraft 26.1.2, Java 25
- Block stores 1 block type at a time (unlimited count)
- Normal right-click: store/retrieve **1** item; Shift + right-click: store/retrieve **full stack** (or up to maxStackSize)
- Drawer accepts any item **without durability** (blocks, potions, etc.) — items with durability (tools, armor, weapons) are rejected
- Prefer terse, factual updates; preserve exact file paths, commands, and error strings

## Progress
### Done
- **playerDestroy** — drops stored items when block is broken (non-creative only)
- **Comparator output** — `hasAnalogOutputSignal` + `getAnalogOutputSignal` return proportional fill level (0–15 based on count/64)
- **26.1.2 API fixes** —
  - `playerWillDestroy` signature: `(Level, BlockPos, BlockState, Player)` returns `BlockState`
  - `getCloneItemStack` signature: `(LevelReader, BlockPos, BlockState, boolean includeData)`
  - `appendHoverText` signature: `(ItemStack, TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)`
  - `CompoundTag.getInt(String)` returns `Optional<Integer>`
  - `BlockEntity.saveAdditional(ValueOutput)` / `loadAdditional(ValueInput)` are the correct override targets
  - `onLoad()` removed (doesn't exist in 26.1.2)
  - Comparator methods (`hasAnalogOutputSignal`, `getAnalogOutputSignal`) are `protected` in `BlockBehaviour`
  - `Block.destroy(LevelAccessor, BlockPos, BlockState)` is `public` in `Block` class
- **Vanilla flow for shift+click** — `DrawerBlockItem.useOn` IS called when `isSecondaryUseActive() && !stack.isEmpty()`. Vanilla `ServerPlayerGameMode.useItemOn` skips `BlockState.useItemOn` and calls `ItemStack.useOn` directly. The `mayInteract` permission check in `handleUseItemOn` happens BEFORE this, so **claims mods are respected**.
- **DrawerBlockItem.useOn handles shift+click** — stores entire held stack (or retrieves 64 on empty hand). Returns `SUCCESS` on client (prevents block placement, triggers packet send), does actual logic on server.
- **BasicDrawerBlock.useItemOn handles normal click** — stores 1 / retrieves 1 (or 64 if shift+empty-hand, for the empty-hand shift case which goes through `BlockState.useItemOn`)
- **Mixin uses `@ModifyVariable` at `@At("STORE")`** — intercepts the `skipBlockInteraction` boolean AFTER it's been computed and written to local var 9. If skip=true and targeting a drawer, returns false so `BlockState.useItemOn` is called. This lets all HEAD-injected checks (including claims mods like Open Parties & Claims) run first, so permissions are respected.
- **Text rendering** via `submitNameTag` with camera billboard cancellation, font faces +Z
- **submitText doesn't render** despite correct params — root cause unknown
- **canStore** accepts any stack with `getMaxDamage() == 0` (rejects tools/armor/weapons)
- **tryRemove bug fixed** — was clearing `storedItem` before `copyWithCount(taken)`, now saves result first
- **tryRemove capped** at `storedItem.getMaxStackSize()`
- **syncToClient** added — calls `level.sendBlockUpdated(...)` and `setChanged()` after `tryAdd`/`tryRemove`

### In Progress
- (none)

### Todo
- (none)

### Blocked
- `submitText` remains non-functional — renders nothing even with correct params; `submitNameTag` workaround used instead

## Key Decisions
- No mixin for interaction handling — use vanilla `ItemStack.useOn` path which already exists for shift+click
- `DrawerBlockItem.useOn` for shift+click logic (store all / retrieve 64); `BasicDrawerBlock.useItemOn` for normal-click logic (store 1 / retrieve 1)
- Permission checks (`mayInteract`) run in `handleUseItemOn` before `useItemOn` → no permission bypass
- Use `submitNameTag` with camera billboard cancellation instead of `submitText` (which doesn't render)
- `stack.getMaxDamage() == 0` to reject durability items
- `ItemStack.isSameItemSameComponents()` for type matching

## Relevant Files
- `src/main/java/com/satanas1275/easydrawers/item/DrawerBlockItem.java`: handles shift+click store/retrieve (store all / retrieve 64)
- `src/main/java/com/satanas1275/easydrawers/block/BasicDrawerBlock.java`: handles normal-click store/retrieve (store 1 / retrieve 1)
- `src/main/java/com/satanas1275/easydrawers/block/DrawerBlockEntity.java`: storage, NBT, add/remove logic with syncToClient
- `src/main/java/com/satanas1275/easydrawers/client/render/DrawerRenderState.java`: render DTO
- `src/main/java/com/satanas1275/easydrawers/client/render/DrawerBlockEntityRenderer.java`: face item rendering + text via submitNameTag
- `src/main/java/com/satanas1275/easydrawers/EasyDrawers.java`: registration
- `src/main/java/com/satanas1275/easydrawers/EasyDrawersClient.java`: BER registration
- `easydrawers.mixins.json`: mixin config (no active mixins currently)
