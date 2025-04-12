"use client";

import { useState } from "react";
import { Header } from "@/components/header";
import { AddressSearch } from "@/components/address-search";
import { PharmacyCard } from "@/components/pharmacy-card";
import { PharmacySkeleton } from "@/components/pharmacy-skeleton";
import { searchPharmaciesByAddress, type PharmacyDirection } from "@/lib/api";

export default function Home() {
  const [searchAddress, setSearchAddress] = useState<string>("");
  const [pharmacies, setPharmacies] = useState<PharmacyDirection[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [searched, setSearched] = useState<boolean>(false);

  const handleAddressSelect = async (address: string) => {
    setSearchAddress(address);
    if (!address) return;

    setLoading(true);
    setSearched(true);

    try {
      const result = await searchPharmaciesByAddress(address);
      setPharmacies(result);
    } catch (error) {
      console.error("약국 검색 오류:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen flex-col bg-gray-50">
      <Header />
      <main className="flex-1 px-4 py-8 md:px-6 lg:px-8">
        <div className="mx-auto max-w-7xl">
          <section className="rounded-lg bg-white shadow-sm p-6 md:p-8 mb-8">
            <div className="max-w-3xl mx-auto space-y-4 mb-8">
              <h1 className="text-3xl md:text-4xl font-bold text-center text-gray-900">
                가까운 약국 찾기
              </h1>
              <p className="text-muted-foreground text-center text-lg">
                주소를 입력하시면 가장 가까운 약국을 추천해 드립니다.
              </p>
              <div className="mt-8">
                <AddressSearch onAddressSelect={handleAddressSelect} />
              </div>
            </div>
          </section>

          {searched && (
            <section className="rounded-lg bg-white shadow-sm p-6 md:p-8">
              <h2 className="text-xl md:text-2xl font-semibold mb-6 pb-4 border-b">
                {loading
                  ? "검색 중..."
                  : pharmacies.length > 0
                  ? `${searchAddress} 주변 약국 ${pharmacies.length}곳`
                  : `${searchAddress} 주변에 추천 약국이 없습니다.`}
              </h2>

              <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
                {loading ? (
                  <>
                    <PharmacySkeleton />
                    <PharmacySkeleton />
                    <PharmacySkeleton />
                  </>
                ) : (
                  pharmacies.map((pharmacy) => (
                    <PharmacyCard
                      key={pharmacy.pharmacyName}
                      pharmacy={pharmacy}
                    />
                  ))
                )}
              </div>
            </section>
          )}
        </div>
      </main>
      <footer className="border-t bg-white py-8">
        <div className="container max-w-7xl mx-auto px-4 md:px-6 lg:px-8">
          <div className="flex flex-col items-center justify-between gap-4 md:flex-row">
            <p className="text-center text-sm text-muted-foreground">
              &copy; {new Date().getFullYear()} 약국 추천 서비스. All rights
              reserved.
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
}
