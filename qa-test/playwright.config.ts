import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 120_000,
  expect: { timeout: 8_000 },
  fullyParallel: true,
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    trace: 'retain-on-failure',
  },
  projects: [

    {
      name: 'mobile-ui',
      testMatch: /03-.*\.spec\.ts/,
      use: { ...devices['Pixel 7'] },
    },
    // Bagian 2: website live
    {
      name: 'website-desktop',
      testMatch: /02-.*\.spec\.ts/,
      use: { ...devices['Desktop Chrome'], baseURL: 'https://borobudurmarathon.com' },
    },
    {
      name: 'website-mobile',
      testMatch: /02-.*\.spec\.ts/,
      use: { ...devices['Pixel 7'], baseURL: 'https://borobudurmarathon.com' },
    },
  ],
});
