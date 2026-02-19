using System;
using System.Collections.Generic;
using System.Text.Json.Serialization;

namespace LKS_ITSSA_2025.Models;

public partial class User
{
    public int Id { get; set; }

    public string? Nama { get; set; }

    public string? EncryptBiometric { get; set; }

    public int? KodeReveral { get; set; }

    public string? Email { get; set; }

    public string? Password { get; set; }
    public string? ProfilPict { get; set; }

    [JsonIgnore]
    public virtual ICollection<AbsenUser> AbsenUsers { get; set; } = new List<AbsenUser>();

    [JsonIgnore]
    public virtual ICollection<Gaji> Gajis { get; set; } = new List<Gaji>();

    [JsonIgnore]
    public virtual KodeReveral? KodeReveralNavigation { get; set; }
    [JsonIgnore]
    public virtual ICollection<KodeReveral> KodeReverals { get; set; } = new List<KodeReveral>();
    [JsonIgnore]
    public virtual ICollection<Penggajian> Penggajians { get; set; } = new List<Penggajian>();
    [JsonIgnore]
    public virtual ICollection<TaskingProgress> TaskingProgresses { get; set; } = new List<TaskingProgress>();
}
