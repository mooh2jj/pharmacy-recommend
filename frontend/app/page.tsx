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
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="flex-1">
        <section className="container py-10 space-y-8">
          <div className="space-y-2">
            <h1 className="text-3xl font-bold">가까운 약국 찾기</h1>
            <p className="text-muted-foreground">
              주소를 입력하시면 가장 가까운 약국을 추천해 드립니다.
            </p>
          </div>

          <AddressSearch onAddressSelect={handleAddressSelect} />

          {searched && (
            <div className="space-y-4">
              <h2 className="text-xl font-semibold">
                {loading
                  ? "검색 중..."
                  : pharmacies.length > 0
                  ? `${searchAddress} 주변 약국 ${pharmacies.length}곳`
                  : `${searchAddress} 주변에 추천 약국이 없습니다.`}
              </h2>

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
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
            </div>
          )}
        </section>
      </main>
      <footer className="border-t py-6">
        <div className="container flex flex-col items-center justify-between gap-4 md:flex-row">
          <p className="text-center text-sm text-muted-foreground">
            &copy; {new Date().getFullYear()} 약국 추천 서비스. All rights
            reserved.
          </p>
        </div>
      </footer>
    </div>
  );
}
