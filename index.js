import React from 'react';
import { registerRootComponent } from 'expo';
import { requireNativeViewManager } from 'expo-modules-core';

const PlayerView = requireNativeViewManager('BytePlusPlayer');

function App() {
  return <PlayerView style={{ flex: 1 }} />;
}

registerRootComponent(App);
