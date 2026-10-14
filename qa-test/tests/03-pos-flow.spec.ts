import { test, expect } from '@playwright/test';
import { PosPage, PRICE, rp, ItemId } from '../pages/PosPage';

/**
 * Bagian 3 - Flow chart POS
 * Aturan yang diasumsikan (lihat README): diskon 10% bila subtotal >= Rp100.000 dan kasir
 * menekan "Apply Discount/Promotion"; split payment = 40% tunai, 60% QRIS (tunai dibulatkan,
 * QRIS = sisa) sehingga tunai + QRIS selalu = total.
 */
const DISC_RATE = 0.1;
const expectedDiscount = (sub: number) => (sub >= 100000 ? Math.round(sub * DISC_RATE) : 0);

test.describe('Bagian 3b - Diskon bila total belanja >= Rp100.000', () => {
  let pos: PosPage;
  test.beforeEach(async ({ page }) => { pos = new PosPage(page); await pos.goto(); });

  test('[TC-POS-001] Keranjang kosong: pesan kosong tampil dan pembayaran ditolak', async ({ page }) => {
    await expect(page.getByText('Cart is empty').first()).toBeVisible();
    await pos.pay.click();
    await expect(pos.payMsg).toHaveText('Cart is empty');
  });

  test('[TC-POS-002] Tambah satu item: subtotal dan total benar', async () => {
    await pos.add('A');
    await expect(pos.subtotal).toHaveText(rp(30000));
    await expect(pos.total).toHaveText(rp(30000));
    await expect(pos.discount).toHaveText(rp(0));
  });

  test('[TC-POS-003] Beberapa item berbeda: subtotal terakumulasi', async () => {
    await pos.add('A'); await pos.add('B');
    await expect(pos.subtotal).toHaveText(rp(PRICE.A + PRICE.B));
  });

  test('[TC-POS-004] Ubah qty (+/-) memperbarui subtotal; qty 0 menghapus item', async ({ page }) => {
    await pos.add('A');
    await pos.inc('A');
    await expect(pos.subtotal).toHaveText(rp(60000));
    await pos.dec('A'); await pos.dec('A');
    await expect(page.getByText('Cart is empty').first()).toBeVisible();
    await expect(pos.subtotal).toHaveText(rp(0));
  });

  test('[TC-POS-005] Subtotal Rp99.999 (batas bawah): tidak ada diskon', async () => {
    await pos.add('E'); await pos.add('F');                    // 49.999 + 50.000
    await expect(pos.subtotal).toHaveText(rp(99999));
    await pos.applyDiscount.click();
    await expect(pos.discountMsg).toContainText('not applied');
    await expect(pos.discount).toHaveText(rp(0));
    await expect(pos.total).toHaveText(rp(99999));
  });

  test('[TC-POS-006] Subtotal tepat Rp100.000 (batas): diskon berlaku', async () => {
    await pos.add('F', 2);
    await expect(pos.subtotal).toHaveText(rp(100000));
    await pos.applyDiscount.click();
    await expect(pos.discountMsg).toContainText('Discount applied');
    await expect(pos.discount).toHaveText(rp(10000));
    await expect(pos.total).toHaveText(rp(90000));
  });

  test('[TC-POS-007] Subtotal di atas batas (A,B,D = Rp135.000): diskon berlaku', async () => {
    await pos.add('A'); await pos.add('B'); await pos.add('D');
    await pos.applyDiscount.click();
    await expect(pos.discount).toHaveText(rp(13500));
    await expect(pos.total).toHaveText(rp(121500));
  });

  test('[TC-POS-008] Beberapa barang dengan qty banyak (A x2, B, C = Rp125.000)', async () => {
    await pos.add('A', 2); await pos.add('B'); await pos.add('C');
    await pos.applyDiscount.click();
    await expect(pos.subtotal).toHaveText(rp(125000));
    await expect(pos.discount).toHaveText(rp(12500));
    await expect(pos.total).toHaveText(rp(112500));
  });

  test('[TC-POS-009] Tanpa menekan Apply Discount tidak ada diskon walau >= Rp100.000', async () => {
    await pos.add('F', 3);
    await expect(pos.discount).toHaveText(rp(0));
    await expect(pos.total).toHaveText(rp(150000));
  });

  test('[TC-POS-010] Diskon otomatis dicabut saat item dikurangi hingga < Rp100.000', async () => {
    await pos.add('F', 2);
    await pos.applyDiscount.click();
    await expect(pos.discount).toHaveText(rp(10000));
    await pos.remove('F');                                       // keranjang kosong
    await pos.add('A');
    await expect(pos.discount).toHaveText(rp(0));
    await expect(pos.total).toHaveText(rp(30000));
  });

  test('[TC-POS-011] Awalnya < Rp100.000, ditambah item hingga >= Rp100.000 lalu Apply', async () => {
    await pos.add('A');                                          // 30.000
    await pos.applyDiscount.click();
    await expect(pos.discount).toHaveText(rp(0));
    await pos.add('D'); await pos.add('C');                      // 110.000
    await pos.applyDiscount.click();
    await expect(pos.discount).toHaveText(rp(11000));
    await expect(pos.total).toHaveText(rp(99000));
  });

  test('[TC-POS-012] Apply Discount pada keranjang kosong ditolak dengan pesan', async () => {
    await pos.applyDiscount.click();
    await expect(pos.discountMsg).toHaveText('Cart is empty');
  });

  // Data-driven: berbagai kombinasi di sekitar ambang
  const combos: Array<[string, Partial<Record<ItemId, number>>]> = [
    ['A,B (75.000)', { A: 1, B: 1 }],
    ['E,F (99.999)', { E: 1, F: 1 }],
    ['F x2 (100.000)', { F: 2 }],
    ['C x5 (100.000)', { C: 5 }],
    ['D,E (109.999)', { D: 1, E: 1 }],
    ['A,B,C,D (155.000)', { A: 1, B: 1, C: 1, D: 1 }],
  ];
  combos.forEach(([name, items], i) => {
    test(`[TC-POS-013.${i + 1}] Kombinasi ${name}: diskon sesuai aturan ambang`, async () => {
      let sub = 0;
      for (const [id, q] of Object.entries(items) as [ItemId, number][]) { await pos.add(id, q); sub += PRICE[id] * q; }
      await pos.applyDiscount.click();
      const d = expectedDiscount(sub);
      await expect(pos.subtotal).toHaveText(rp(sub));
      await expect(pos.discount).toHaveText(rp(d));
      await expect(pos.total).toHaveText(rp(sub - d));
    });
  });
});

test.describe('Bagian 3a - Split payment 40:60 (Tunai : QRIS)', () => {
  let pos: PosPage;
  test.beforeEach(async ({ page }) => { pos = new PosPage(page); await pos.goto(); });

  test('[TC-POS-020] Pilih Split: porsi tunai 40% dan QRIS 60% (total Rp100.000)', async () => {
    await pos.add('F', 2);                                       // 100.000 tanpa Apply
    await pos.chooseMethod('Split');
    await expect(pos.splitCash).toHaveText(rp(40000));
    await expect(pos.splitQris).toHaveText(rp(60000));
  });

  test('[TC-POS-021] Split dihitung dari total setelah diskon (Rp121.500 -> 48.600 / 72.900)', async () => {
    await pos.add('A'); await pos.add('B'); await pos.add('D');
    await pos.applyDiscount.click();
    await pos.chooseMethod('Split');
    await expect(pos.total).toHaveText(rp(121500));
    await expect(pos.splitCash).toHaveText(rp(48600));
    await expect(pos.splitQris).toHaveText(rp(72900));
  });

  test('[TC-POS-022] Pembulatan: tunai + QRIS selalu = total (Rp79.999)', async () => {
    await pos.add('E'); await pos.add('A');                      // 79.999
    await pos.chooseMethod('Split');
    await expect(pos.splitCash).toHaveText(rp(32000));
    await expect(pos.splitQris).toHaveText(rp(47999));
  });

  test('[TC-POS-023] Split sukses: tunai pas + QRIS lunas, struk memuat kedua metode', async () => {
    await pos.add('F', 2);
    await pos.chooseMethod('Split');
    await pos.cashReceived.fill('40000');
    await pos.qrisPaid.click();
    await expect(pos.qrisStatus).toHaveText('QRIS: paid');
    await pos.pay.click();
    await expect(pos.saleStatus).toHaveText('Payment approved');
    await expect(pos.receipt).toContainText(`Cash ${rp(40000)}`);
    await expect(pos.receipt).toContainText(`QRIS ${rp(60000)}`);
    await expect(pos.receipt).toContainText(`Total ${rp(100000)}`);
  });

  test('[TC-POS-024] Tunai melebihi porsi: kembalian benar (50.000 - 40.000 = 10.000)', async () => {
    await pos.add('F', 2);
    await pos.chooseMethod('Split');
    await pos.cashReceived.fill('50000');
    await expect(pos.change).toHaveText(rp(10000));
    await pos.qrisPaid.click();
    await pos.pay.click();
    await expect(pos.receipt).toContainText(`Change ${rp(10000)}`);
  });

  test('[TC-POS-025] Tunai kurang dari porsi 40% ditolak', async () => {
    await pos.add('F', 2);
    await pos.chooseMethod('Split');
    await pos.cashReceived.fill('39999');
    await pos.qrisPaid.click();
    await pos.pay.click();
    await expect(pos.payMsg).toHaveText('Insufficient cash received');
    await expect(pos.saleStatus).toBeHidden();
  });

  test('[TC-POS-026] Tunai cukup tetapi QRIS belum dibayar ditolak', async () => {
    await pos.add('F', 2);
    await pos.chooseMethod('Split');
    await pos.cashReceived.fill('40000');
    await pos.pay.click();
    await expect(pos.payMsg).toHaveText('QRIS payment not completed');
    await expect(pos.saleStatus).toBeHidden();
  });

  test('[TC-POS-027] QRIS gagal lalu retry berhasil', async () => {
    await pos.add('F', 2);
    await pos.chooseMethod('Split');
    await pos.cashReceived.fill('40000');
    await pos.qrisFail.click();
    await pos.pay.click();
    await expect(pos.payMsg).toHaveText('QRIS payment failed, please retry');
    await pos.qrisPaid.click();
    await pos.pay.click();
    await expect(pos.saleStatus).toHaveText('Payment approved');
  });

  test('[TC-POS-028] Tunai tidak diisi (0) pada split ditolak', async () => {
    await pos.add('F', 2);
    await pos.chooseMethod('Split');
    await pos.qrisPaid.click();
    await pos.pay.click();
    await expect(pos.payMsg).toHaveText('Insufficient cash received');
  });

  const splitTotals: Array<[string, Partial<Record<ItemId, number>>]> = [
    ['A (30.000)', { A: 1 }], ['E (49.999)', { E: 1 }], ['E,A (79.999)', { E: 1, A: 1 }],
    ['B x3 (135.000)', { B: 3 }], ['E x3 (149.997)', { E: 3 }],
  ];
  splitTotals.forEach(([name, items], i) => {
    test(`[TC-POS-029.${i + 1}] Invarian split ${name}: tunai + QRIS = total`, async () => {
      for (const [id, q] of Object.entries(items) as [ItemId, number][]) await pos.add(id, q);
      await pos.chooseMethod('Split');
      const num = async (l: typeof pos.total) => Number((await l.innerText()).replace(/\D/g, ''));
      const [t, c, q] = [await num(pos.total), await num(pos.splitCash), await num(pos.splitQris)];
      expect(c + q).toBe(t);
      expect(c).toBe(Math.round(t * 0.4));
    });
  });

  test('[TC-POS-030] Ganti metode dari Split ke QRIS penuh: status QRIS di-reset', async () => {
    await pos.add('F', 2);
    await pos.chooseMethod('Split');
    await pos.qrisPaid.click();
    await expect(pos.qrisStatus).toHaveText('QRIS: paid');
    await pos.chooseMethod('QRIS');
    await expect(pos.qrisStatus).toHaveText('QRIS: waiting');
    await pos.pay.click();
    await expect(pos.payMsg).toHaveText('QRIS payment not completed');
  });

  test('[TC-POS-031] Tombol Process Payment nonaktif setelah sukses (cegah bayar ganda)', async () => {
    await pos.add('F', 2);
    await pos.chooseMethod('Split');
    await pos.cashReceived.fill('40000');
    await pos.qrisPaid.click();
    await pos.pay.click();
    await expect(pos.saleStatus).toBeVisible();
    await expect(pos.pay).toBeDisabled();
  });
});

test.describe('Alur lengkap sesuai flow chart & struk', () => {
  let pos: PosPage;
  test.beforeEach(async ({ page }) => { pos = new PosPage(page); await pos.goto(); });

  test('[TC-POS-040] E2E dengan diskon + split 40:60 + cetak & kirim struk digital', async () => {
    await pos.add('A', 2); await pos.add('B'); await pos.add('C');   // 125.000 -> diskon 12.500 -> 112.500
    await pos.applyDiscount.click();
    await pos.chooseMethod('Split');
    await expect(pos.splitCash).toHaveText(rp(45000));
    await expect(pos.splitQris).toHaveText(rp(67500));
    await pos.cashReceived.fill('50000');
    await pos.qrisPaid.click();
    await pos.pay.click();
    await expect(pos.saleStatus).toHaveText('Payment approved');
    await expect(pos.receipt).toContainText(`Discount ${rp(12500)}`);
    await expect(pos.receipt).toContainText(`Total ${rp(112500)}`);
    await pos.print.click();
    await expect(pos.receiptMsg).toHaveText('Receipt sent to printer');
    await pos.receiptEmail.fill('pelanggan@example.com');
    await pos.sendDigital.click();
    await expect(pos.receiptMsg).toHaveText('Digital receipt sent to pelanggan@example.com');
  });

  test('[TC-POS-041] E2E tanpa diskon (jalur "No") dengan tunai penuh', async () => {
    await pos.add('A');
    await pos.applyDiscount.click();                                 // 30.000 < 100.000 -> No
    await pos.cashReceived.fill('50000');
    await expect(pos.change).toHaveText(rp(20000));
    await pos.pay.click();
    await expect(pos.saleStatus).toHaveText('Payment approved');
    await expect(pos.receipt).toContainText(`Discount ${rp(0)}`);
  });

  test('[TC-POS-042] Pembayaran QRIS penuh berhasil', async () => {
    await pos.add('F', 2);
    await pos.chooseMethod('QRIS');
    await pos.qrisPaid.click();
    await pos.pay.click();
    await expect(pos.saleStatus).toHaveText('Payment approved');
    await expect(pos.receipt).toContainText(`QRIS ${rp(100000)}`);
  });

  test('[TC-POS-043] Struk digital: email tidak valid ditolak', async () => {
    await pos.add('F', 2);
    await pos.cashReceived.fill('100000');
    await pos.pay.click();
    await pos.receiptEmail.fill('bukan-email');
    await pos.sendDigital.click();
    await expect(pos.receiptMsg).toHaveText('Invalid email');
  });
});
