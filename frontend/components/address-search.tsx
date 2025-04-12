"use client";

import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Script from "next/script";
import { Search, MapPin } from "lucide-react";

declare global {
  interface Window {
    daum: {
      Postcode: new (options: {
        oncomplete: (data: {
          address: string;
          addressType: string;
          bname: string;
          buildingName: string;
          zonecode: string;
          jibunAddress?: string;
          roadAddress?: string;
        }) => void;
      }) => {
        open: () => void;
      };
    };
  }
}

interface AddressSearchProps {
  onAddressSelect: (address: string) => void;
}

export function AddressSearch({ onAddressSelect }: AddressSearchProps) {
  const [address, setAddress] = useState<string>("");
  const [scriptLoaded, setScriptLoaded] = useState<boolean>(false);

  const openPostcode = () => {
    if (!scriptLoaded) {
      alert("주소 검색 서비스가 로드 중입니다. 잠시만 기다려주세요.");
      return;
    }

    new window.daum.Postcode({
      oncomplete: function (data) {
        const fullAddress = data.address;
        setAddress(fullAddress);
        onAddressSelect(fullAddress);
      },
    }).open();
  };

  const handleSearch = () => {
    if (!address.trim()) {
      alert("주소를 입력해주세요.");
      return;
    }
    onAddressSelect(address);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setAddress(e.target.value);
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") {
      handleSearch();
    }
  };

  return (
    <div className="space-y-4">
      <Script
        src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"
        onLoad={() => setScriptLoaded(true)}
        strategy="lazyOnload"
      />
      <div className="relative w-full max-w-2xl mx-auto">
        <div className="flex flex-col sm:flex-row gap-2">
          <div className="relative flex-1">
            <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
              <MapPin className="h-5 w-5 text-muted-foreground" />
            </div>
            <Input
              type="text"
              placeholder="주소 입력"
              value={address}
              onChange={handleInputChange}
              onKeyDown={handleKeyPress}
              className="pl-10 h-12 text-base"
            />
          </div>
          <div className="flex gap-2">
            <Button
              onClick={openPostcode}
              variant="outline"
              className="whitespace-nowrap h-12"
            >
              주소 검색
            </Button>
            <Button
              onClick={handleSearch}
              className="whitespace-nowrap h-12 px-6 font-semibold"
            >
              <Search className="mr-2 h-5 w-5" />
              약국 찾기
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
