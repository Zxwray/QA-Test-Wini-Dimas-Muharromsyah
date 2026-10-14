import { test, expect, Page } from '@playwright/test';

/**
 * Bagian 2 - Live testing https://borobudurmarathon.com/
 * Selector disusun dari HTML halaman yang dibaca (WordPress + Polylang, menu EN/ID).
 * Tag @bug-candidate = tes yang memang diharapkan GAGAL bila temuan bug masih ada
 * (lihat sheet "Bug Report" di QA_Test_Cases.xlsx).
 */
const NAV_PAGES: Array<[string, string]> = [
  ['Race Category', '/bank-jateng-borobudur-marathon-2026/'],
  ['Rules & Regulations', '/rules-regulations/'],
  ['Prizes', '/prizes-2026/'],
  ['FAQ', '/faq/'],
  ['Race Results', '/race-results/'],
  ['Bank Jateng Friendship Run', '/friendship-run-program/'],
  ['Bank Jateng Young Talent', '/id/young-talent-2026/'],
  ['Gallery', '/gallery/'],
  ['Race Guide', '/race-guide/'],
  ['Sponsors', '/sponsors/'],
  ['Contact Us', '/contact/'],
  ['Apps', '/apps/'],
  ['Merchandise', '/official-merchandise/'],
];

const gotoOk = async (page: Page, url: string) => {
  const res = await page.goto(url, { waitUntil: 'domcontentloaded' });
  expect(res, `no response for ${url}`).not.toBeNull();
  return res!;
};

test.describe('Bagian 2 - Website Borobudur Marathon', () => {
  // ---------- Home ----------
  test('[TC-WEB-001] Home dimuat (HTTP 200) dan title memuat "Borobudur Marathon"', async ({ page }) => {
    const res = await gotoOk(page, '/');
    expect(res.status()).toBe(200);
    await expect(page).toHaveTitle(/Borobudur Marathon/i);
  });

  test('[TC-WEB-002] Hero "DECADE OF LEGACY" tampil', async ({ page }) => {
    await gotoOk(page, '/');
    await expect(page.getByRole('heading', { name: /DECADE OF LEGACY/i }).first()).toBeVisible();
  });

  test('[TC-WEB-003] Menu utama memuat seluruh item', async ({ page }) => {
    await gotoOk(page, '/');
    for (const label of ['Home', 'The Race', 'Program', 'Media', 'Contact', 'Apps', 'Merchandise']) {
      await expect(page.locator('nav, header').getByText(label, { exact: true }).first()).toBeAttached();
    }
  });

  test('[TC-WEB-004] Setiap submenu mengarah ke URL yang benar', async ({ page }) => {
    await gotoOk(page, '/');
    for (const [label, href] of NAV_PAGES) {
      await expect(page.locator(`a[href$="${href}"]`).first(), `${label} -> ${href}`).toBeAttached();
    }
  });

  test('[TC-WEB-005] Navigasi ke Race Category lewat menu (desktop)', async ({ page, isMobile }) => {
    test.skip(isMobile, 'menu desktop');
    await gotoOk(page, '/');
    await page.getByRole('link', { name: 'The Race' }).first().hover();
    await page.getByRole('link', { name: 'Race Category' }).first().click();
    await expect(page).toHaveURL(/bank-jateng-borobudur-marathon-2026/);
  });

  // ---------- Semua halaman utama dapat dibuka ----------
  for (const [i, [label, href]] of NAV_PAGES.entries()) {
    test(`[TC-WEB-006.${i + 1}] Halaman "${label}" dapat dibuka (HTTP < 400) dan memiliki konten`, async ({ page }) => {
      const res = await gotoOk(page, href);
      expect(res.status(), `${href} status`).toBeLessThan(400);
      await expect(page.locator('body')).not.toBeEmpty();
      await expect(page.locator('h1, h2').first()).toBeVisible();
    });
  }

  // ---------- Bahasa ----------
  test('[TC-WEB-007] Ganti bahasa ke Bahasa Indonesia (/id/)', async ({ page }) => {
    await gotoOk(page, '/');
    await page.locator('a[href$="/id/"]').first().click({ force: true });
    await expect(page).toHaveURL(/\/id\/?$/);
    await expect(page.locator('html')).toHaveAttribute('lang', /id/);
  });

  test('[TC-WEB-008] Kembali ke English dari versi Indonesia', async ({ page }) => {
    await gotoOk(page, '/id/');
    await page.locator('a[hreflang^="en"], a[href="https://borobudurmarathon.com/"]').first().click({ force: true });
    await expect(page).toHaveURL(/borobudurmarathon\.com\/?$/);
    await expect(page.locator('html')).toHaveAttribute('lang', /en/);
  });

  // ---------- Registrasi / tautan eksternal ----------
  test('[TC-WEB-009] Tautan registrasi mengarah ke my.borobudurmarathon.com', async ({ page, request }) => {
    await gotoOk(page, '/');
    const link = page.locator('a[href*="my.borobudurmarathon.com"]').first();
    await expect(link).toBeAttached();
    const res = await request.get('https://my.borobudurmarathon.com/', { maxRedirects: 5 });
    expect(res.status()).toBeLessThan(400);
  });

  test('[TC-WEB-010] Footer: email kontak dan 4 tautan media sosial', async ({ page }) => {
    await gotoOk(page, '/');
    await expect(page.getByRole('link', { name: 'info@borobudurmarathon.com' })).toBeAttached();
    for (const host of ['facebook.com', 'twitter.com', 'instagram.com', 'youtube.com']) {
      await expect(page.locator(`footer a[href*="${host}"]`).first(), host).toBeAttached();
    }
  });

  // ---------- Kualitas ----------
  test('[TC-WEB-012] Tidak ada gambar rusak di Home', async ({ page }) => {
    await gotoOk(page, '/');
    await page.evaluate(async () => {
      for (let y = 0, i = 0; y < document.body.scrollHeight && i < 60; y += 600, i++) {
        window.scrollTo(0, y);
        await new Promise(r => setTimeout(r, 120));
      }
    });
    await page.waitForLoadState('load', { timeout: 30_000 }).catch(() => undefined);
    const broken = await page.$$eval('img', (imgs) =>
      imgs.filter(i => i.complete && i.naturalWidth === 0 && i.currentSrc && !i.src.startsWith('data:')).map(i => i.currentSrc));
    expect(broken, `gambar rusak: ${broken.join(', ')}`).toEqual([]);
  });

  test('[TC-WEB-013] Tidak ada error JavaScript di console saat memuat Home', async ({ page }) => {
    const errors: string[] = [];
    page.on('pageerror', (e) => errors.push(e.message));
    await gotoOk(page, '/');
    await page.waitForLoadState('networkidle').catch(() => undefined);
    expect(errors).toEqual([]);
  });

  test('[TC-WEB-014] Performa: Home selesai dimuat < 10 detik', async ({ page }) => {
    const t0 = Date.now();
    await page.goto('/', { waitUntil: 'load' });
    expect(Date.now() - t0).toBeLessThan(10_000);
  });

  test('[TC-WEB-015] Responsif: tanpa horizontal scroll di Home', async ({ page }) => {
    await gotoOk(page, '/');
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth + 1);
    expect(overflow).toBe(false);
  });

  test('[TC-WEB-017] URL tidak ada menampilkan halaman 404 (status 404)', async ({ page }) => {
    const res = await gotoOk(page, '/halaman-tidak-ada-qa-test-xyz/');
    expect(res.status()).toBe(404);
  });

  test('[TC-WEB-018] HTTP dialihkan ke HTTPS', async ({ page }) => {
    await page.goto('http://borobudurmarathon.com/', { waitUntil: 'domcontentloaded' });
    expect(page.url().startsWith('https://')).toBe(true);
  });

  test('[TC-WEB-024] SEO dasar: meta description, canonical, h1 tunggal', async ({ page }) => {
    await gotoOk(page, '/');
    await expect(page.locator('meta[name="description"]')).toHaveAttribute('content', /.+/);
    await expect(page.locator('link[rel="canonical"]')).toHaveCount(1);
    expect(await page.locator('h1').count()).toBe(1);
  });

  // ---------- Kandidat bug (diharapkan gagal sampai diperbaiki) ----------
  test('[TC-WEB-019] @bug-candidate Meta/OG konsisten dengan tahun 2026 (BUG-001)', async ({ page }) => {
    await gotoOk(page, '/');
    const siteName = await page.locator('meta[property="og:site_name"]').getAttribute('content');
    const desc = await page.locator('meta[name="description"]').getAttribute('content');
    expect(siteName ?? '').not.toMatch(/2025/);
    expect(desc ?? '').not.toMatch(/2025/);
  });

  test('[TC-WEB-020] @bug-candidate FAQ tidak memuat informasi event 2022 (BUG-002)', async ({ page }) => {
    await gotoOk(page, '/faq/');
    const text = await page.locator('body').innerText();
    expect(text).not.toMatch(/Borobudur Marathon 2022|November 2022|June 2022/i);
  });

  test('[TC-WEB-026] @bug-candidate Logo sponsor memiliki atribut alt (BUG-003)', async ({ page }) => {
    await gotoOk(page, '/');
    const noAlt = await page.$$eval('img', (imgs) =>
      imgs.filter(i => i.closest('a') && !(i.getAttribute('alt') || '').trim()).length);
    expect(noAlt, 'jumlah gambar bertaut tanpa alt').toBe(0);
  });

  // ---------- Contact form (bersyarat: form belum terverifikasi) ----------
  test('[TC-WEB-021] Contact: kirim form kosong menampilkan validasi', async ({ page }) => {
    await gotoOk(page, '/contact/');
    const form = page.locator('form').first();
    test.skip(!(await form.count()), 'Form kontak tidak ditemukan - verifikasi manual');
    await form.getByRole('button', { name: /send|submit|kirim/i }).click();
    await expect(page.locator('.wpcf7-not-valid-tip, [aria-invalid="true"], :invalid').first()).toBeAttached();
  });
});
