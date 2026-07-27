// Tensura Abyss endgame progression.
// Super Diamond was removed: every ingredient below is obtained in the Shadow Abyss.
ServerEvents.recipes(event => {
  event.remove({ id: 'tensurapack:super_diamond_craft' })
  event.remove({ output: 'kubejs:super_diamond' })
  event.remove({ output: 'kubejs:breath_attunement_crystal' })

  event.shaped('tensura_abyss:condensed_dark_matter', [
    'SAS',
    'ACA',
    'SAS'
  ], {
    S: 'tensura_abyss:dark_slime',
    A: 'tensura_abyss:dark_aether',
    C: 'tensura_abyss:magicule_spire_crystal'
  }).id('tensura_abyss:condensed_dark_matter')

  event.shaped('2x tensura_abyss:abyssal_netherite_ingot', [
    'MNM',
    'NAN',
    'MNM'
  ], {
    M: 'tensura_abyss:condensed_dark_matter',
    N: 'minecraft:netherite_ingot',
    A: 'tensura_abyss:dark_aether'
  }).id('tensura_abyss:abyssal_netherite_ingot')

  const upgrades = {
    sword: 'tensura_abyss:abyssal_netherite_sword',
    helmet: 'tensura_abyss:abyssal_netherite_helmet',
    chestplate: 'tensura_abyss:abyssal_netherite_chestplate',
    leggings: 'tensura_abyss:abyssal_netherite_leggings',
    boots: 'tensura_abyss:abyssal_netherite_boots'
  }

  Object.keys(upgrades).forEach(piece => {
    event.shaped(upgrades[piece], [
      ' I ',
      'IBI',
      ' I '
    ], {
      I: 'tensura_abyss:abyssal_netherite_ingot',
      B: `minecraft:netherite_${piece}`
    }).id(`tensura_abyss:upgrade_${piece}`)
  })
})

ItemEvents.tooltip(event => {
  event.add('tensura_abyss:magicule_spire_crystal', [
    Text.darkPurple('Grows only in deep Shadow Abyss strata.'),
    Text.gray('A required catalyst for Abyssal Netherite.')
  ])
})
