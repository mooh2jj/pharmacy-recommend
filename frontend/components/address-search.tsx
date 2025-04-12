"use client";

import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Script from "next/script";

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
      <div className="flex gap-2">
        <Input
          type="text"
          placeholder="주소 입력"
          value={address}
          onChange={handleInputChange}
          onKeyDown={handleKeyPress}
          className="flex-1"
        />
        <Button onClick={openPostcode} variant="outline">
          주소 검색
        </Button>
        <Button onClick={handleSearch}>약국 찾기</Button>
      </div>
    </div>
  );
}
