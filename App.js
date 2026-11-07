import React from 'react';
import { requireNativeViewManager } from 'expo-modules-core';

const PlayerView = requireNativeViewManager('BytePlusPlayer');

export function App() {
  return <PlayerView style={{ flex: 1 }} />;
}
